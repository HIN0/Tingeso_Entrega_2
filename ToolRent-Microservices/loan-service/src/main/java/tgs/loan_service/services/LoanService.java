package tgs.loan_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tgs.loan_service.entitites.LoanEntity;

import tgs.loan_service.models.ClientDTO;
import tgs.loan_service.models.ToolDTO;
import tgs.loan_service.models.TariffDTO;
import tgs.loan_service.models.KardexDTO;
import tgs.loan_service.models.LoanDetailDTO;
import tgs.loan_service.repositories.LoanRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Nombres de servicio (Asegúrate que coincidan con docker-compose/k8s)
    private final String INVENTORY_URL = "http://inventory-service/api/tools";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    private final String KARDEX_URL = "http://kardex-service/api/kardex";
    private final String TARIFF_URL = "http://tariff-service/api/tariffs";

    public List<LoanEntity> getAllLoans() { return loanRepository.findAll(); }

    public List<LoanEntity> getActiveLoans() {
        return loanRepository.findAllByStatus("ACTIVE");
    }

    public LoanEntity createLoan(Long clientId, Long toolId, String username, LocalDate deadlineDate) {
        // 1. Validaciones
        ClientDTO client = restTemplate.getForObject(CUSTOMER_URL + "/" + clientId, ClientDTO.class);
        if (client == null) throw new RuntimeException("Cliente no existe");
        
        if ("RESTRICTED".equals(client.getStatus())) {
            throw new RuntimeException("Cliente RESTRINGIDO por deudas pendientes.");
        }

        if (loanRepository.findByClientIdAndStatus(clientId, "ACTIVE").size() >= 5) 
            throw new RuntimeException("Cliente excede límite de 5 préstamos.");

        if (loanRepository.existsByClientIdAndToolIdAndStatus(clientId, toolId, "ACTIVE")) {
            throw new RuntimeException("El cliente ya tiene esta herramienta en préstamo.");
        }

        ToolDTO tool = restTemplate.getForObject(INVENTORY_URL + "/" + toolId, ToolDTO.class);
        if (tool == null || tool.getStock() < 1) throw new RuntimeException("Sin stock disponible.");

        if (deadlineDate == null) {
            deadlineDate = LocalDate.now().plusDays(7); 
        }
        if (deadlineDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de devolución no puede ser anterior a hoy.");
        }

        // 2. Descontar Stock (Con skipKardex=true para que Inventory NO reporte, reportamos nosotros)
        try {
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=-1&username=" + username + "&skipKardex=true", null);
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con Inventario: " + e.getMessage());
        }

        // 3. Guardar Préstamo
        try {
            LoanEntity newLoan = LoanEntity.builder()
                    .loanDate(LocalDate.now())
                    .deadlineDate(deadlineDate)
                    .status("ACTIVE")
                    .clientId(clientId)
                    .toolId(toolId)
                    .build();
            
            LoanEntity saved = loanRepository.save(newLoan);
            
            // Registramos el Kardex (Si falla, el préstamo ya se guardó, pero veremos el error en logs)
            registerKardex(toolId, "PRESTAMO", 1, username);
            return saved;
            
        } catch (Exception e) {
            // ROLLBACK MANUAL
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=1&username=" + username + "&skipKardex=true", null);
            throw new RuntimeException("Error interno. Préstamo cancelado.");
        }
    }

    public LoanEntity returnLoan(Long loanId, String username, String condition) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!"ACTIVE".equals(loan.getStatus())) throw new RuntimeException("Préstamo ya cerrado");

        // 1. Obtener Tarifas
        TariffDTO tariff = getTariffs();
        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        // Cálculos
        long daysRented = ChronoUnit.DAYS.between(loan.getLoanDate(), returnDate);
        if (daysRented < 1) daysRented = 1;

        long daysOverdue = 0;
        if (returnDate.isAfter(loan.getDeadlineDate())) {
            daysOverdue = ChronoUnit.DAYS.between(loan.getDeadlineDate(), returnDate);
        }

        double totalToPay = (daysRented * tariff.getDailyRentFee()) + (daysOverdue * tariff.getDailyLateFee());

        // 2. Lógica de Daños
        if ("DAMAGED".equalsIgnoreCase(condition)) {
            totalToPay += tariff.getRepairFee();
            // Inventory cambia estado -> Genera registro Kardex propio (STATUS_CHANGE)
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/status?newStatus=REPAIRING&username=" + username, null);
        } 
        else if ("DESTROYED".equalsIgnoreCase(condition)) {
            ToolDTO tool = restTemplate.getForObject(INVENTORY_URL + "/" + loan.getToolId(), ToolDTO.class);
            if (tool != null && tool.getReplacementValue() != null) {
                totalToPay += tool.getReplacementValue(); 
            } else {
                totalToPay += 100000; 
            }
            // Inventory cambia estado -> Genera registro Kardex propio (STATUS_CHANGE)
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/status?newStatus=DECOMMISSIONED&username=" + username, null);
        }
        else {
            // "GOOD": Devolver stock normal
            // CORRECCIÓN: Agregar &skipKardex=true para evitar duplicado con Inventory
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/stock?quantity=1&username=" + username + "&skipKardex=true", null);
        }

        // 3. Registrar Deuda
        if (totalToPay > 0) {
            restTemplate.put(CUSTOMER_URL + "/" + loan.getClientId() + "/balance?amount=" + totalToPay, null);
        }

        loan.setStatus("RETURNED");
        
        // Registrar devolución comercial en Kardex
        registerKardex(loan.getToolId(), "DEVOLUCION_" + condition, 1, username);
        
        return loanRepository.save(loan);
    }

    private TariffDTO getTariffs() {
        try {
            TariffDTO t = restTemplate.getForObject(TARIFF_URL, TariffDTO.class);
            return (t != null) ? t : new TariffDTO(1500, 5000, 10000);
        } catch (Exception e) {
            return new TariffDTO(1500, 5000, 10000);
        }
    }

    private void registerKardex(Long toolId, String type, int qty, String user) {
        try {
            KardexDTO kardexRequest = new KardexDTO(type, toolId, qty, user);
            restTemplate.postForObject(KARDEX_URL, kardexRequest, Void.class);
        } catch (Exception e) {
            // Loguear el error completo para depuración
            System.err.println("!!! ERROR CRÍTICO REPORTANDO A KARDEX !!!");
            System.err.println("URL intentada: " + KARDEX_URL);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<LoanDetailDTO> findAllWithDetails() {
        List<LoanEntity> loans = loanRepository.findAll();
        return loans.stream().map(loan -> {
            LoanDetailDTO dto = new LoanDetailDTO();
            dto.setId(loan.getId());
            dto.setLoanDate(loan.getLoanDate());
            dto.setDeadlineDate(loan.getDeadlineDate());
            dto.setReturnDate(loan.getReturnDate());
            dto.setStatus(loan.getStatus());
            dto.setClientId(loan.getClientId());
            dto.setToolId(loan.getToolId());

            try {
                ClientDTO client = restTemplate.getForObject("http://customer-service/api/clients/" + loan.getClientId(), ClientDTO.class);
                dto.setClientName(client != null ? client.getName() : "Desconocido");
            } catch (Exception e) {
                dto.setClientName("Error al cargar cliente");
            }

            try {
                ToolDTO tool = restTemplate.getForObject("http://inventory-service/api/tools/" + loan.getToolId(), ToolDTO.class);
                dto.setToolName(tool != null ? tool.getName() : "Desconocido");
            } catch (Exception e) {
                dto.setToolName("Error al cargar herramienta");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}