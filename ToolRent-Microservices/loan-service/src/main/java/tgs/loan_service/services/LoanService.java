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

    // Nombres de servicio (Service Discovery)
    private final String INVENTORY_URL = "http://inventory-service/api/tools";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    private final String KARDEX_URL = "http://kardex-service/api/kardex";
    private final String TARIFF_URL = "http://tariff-service/api/tariffs";

    public List<LoanEntity> getAllLoans() { return loanRepository.findAll(); }

    public LoanEntity createLoan(Long clientId, Long toolId, String username) {
        // 1. Validaciones
        ClientDTO client = restTemplate.getForObject(CUSTOMER_URL + "/" + clientId, ClientDTO.class);
        if (client == null) throw new RuntimeException("Cliente no existe");
        
        // CORRECCIÓN: Validar contra "RESTRICTED" (inglés)
        if ("RESTRICTED".equals(client.getStatus())) {
            throw new RuntimeException("Cliente RESTRINGIDO por deudas pendientes.");
        }

        // Regla: Máximo 5 préstamos
        if (loanRepository.findByClientIdAndStatus(clientId, "ACTIVE").size() >= 5) 
            throw new RuntimeException("Cliente excede límite de 5 préstamos.");

        ToolDTO tool = restTemplate.getForObject(INVENTORY_URL + "/" + toolId, ToolDTO.class);
        if (tool == null || tool.getStock() < 1) throw new RuntimeException("Sin stock disponible.");

        // 2. Descontar Stock (Llamada síncrona)
        try {
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=-1&username=" + username, null);
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con Inventario: " + e.getMessage());
        }

        // 3. Guardar Préstamo (con Rollback manual si falla)
        try {
            LoanEntity newLoan = LoanEntity.builder()
                    .loanDate(LocalDate.now())
                    .deadlineDate(LocalDate.now().plusDays(7)) // Regla: 7 días por defecto
                    .status("ACTIVE")
                    .clientId(clientId)
                    .toolId(toolId)
                    .build();
            
            LoanEntity saved = loanRepository.save(newLoan);
            registerKardex(toolId, "PRESTAMO", 1, username);
            return saved;
            
        } catch (Exception e) {
            // ROLLBACK MANUAL: Devolver el stock si falla la base de datos local
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=1&username=" + username, null);
            throw new RuntimeException("Error interno. Préstamo cancelado.");
        }
    }

    // CORRECCIÓN MAYOR: returnLoan acepta 'condition' para Reglas de Negocio de Daños
    public LoanEntity returnLoan(Long loanId, String username, String condition) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!"ACTIVE".equals(loan.getStatus())) throw new RuntimeException("Préstamo ya cerrado");

        // 1. Obtener Tarifas
        TariffDTO tariff = getTariffs();
        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        // Cálculos de días
        long daysRented = ChronoUnit.DAYS.between(loan.getLoanDate(), returnDate);
        if (daysRented < 1) daysRented = 1; // Mínimo 1 día

        long daysOverdue = 0;
        if (returnDate.isAfter(loan.getDeadlineDate())) {
            daysOverdue = ChronoUnit.DAYS.between(loan.getDeadlineDate(), returnDate);
        }

        // 2. Calcular Pago
        double totalToPay = (daysRented * tariff.getDailyRentFee()) + (daysOverdue * tariff.getDailyLateFee());

        // 2. Lógica de Daños (condition: GOOD, DAMAGED, DESTROYED)
        if ("DAMAGED".equalsIgnoreCase(condition)) {
            // Regla: Cobrar reparación y marcar herramienta "En Reparación"
            totalToPay += tariff.getRepairFee();
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/status?newStatus=REPAIRING&username=" + username, null);
            // NO se devuelve stock disponible porque está rota
        } 
        else if ("DESTROYED".equalsIgnoreCase(condition)) {
            // Daño irreparable: Se cobra VALOR DE REPOSICIÓN (traído del Inventario)
            ToolDTO tool = restTemplate.getForObject(INVENTORY_URL + "/" + loan.getToolId(), ToolDTO.class);
            
            if (tool != null && tool.getReplacementValue() != null) {
                double replacementCost = tool.getReplacementValue();
                // Usamos el valor de reposición de la herramienta
                totalToPay += replacementCost; 
            } else {
                // Fallback por si el dato viene nulo (seguridad)
                totalToPay += 100000; 
            }

            // Damos de baja la herramienta en inventario
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/status?newStatus=DECOMMISSIONED&username=" + username, null);
        }
        else {
            // "GOOD": Devolver stock normal
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/stock?quantity=1&username=" + username, null);
        }

        // 3. Registrar Deuda en Cliente
        if (totalToPay > 0) {
            restTemplate.put(CUSTOMER_URL + "/" + loan.getClientId() + "/balance?amount=" + totalToPay, null);
        }

        loan.setStatus("RETURNED");
        registerKardex(loan.getToolId(), "DEVOLUCION_" + condition, 1, username);
        
        return loanRepository.save(loan);
    }

    private TariffDTO getTariffs() {
        try {
            TariffDTO t = restTemplate.getForObject(TARIFF_URL, TariffDTO.class);
            return (t != null) ? t : new TariffDTO(1500, 5000, 10000); // Fallback safe
        } catch (Exception e) {
            return new TariffDTO(1500, 5000, 10000);
        }
    }

    private void registerKardex(Long toolId, String type, int qty, String user) {
            try {
                KardexDTO kardexRequest = new KardexDTO(type, toolId, qty, user);
                restTemplate.postForObject(KARDEX_URL, kardexRequest, Void.class);
            } catch (Exception e) {
                System.err.println("Error reportando a Kardex: " + e.getMessage());
            }
        }

    public List<LoanDetailDTO> findAllWithDetails() {
        // 1. Obtener todos los préstamos de la BD local
        List<LoanEntity> loans = loanRepository.findAll();
        
        // 2. Convertir cada entidad a DTO buscando los nombres
        return loans.stream().map(loan -> {
            LoanDetailDTO dto = new LoanDetailDTO();
            dto.setId(loan.getId());
            dto.setLoanDate(loan.getLoanDate());
            dto.setDeadlineDate(loan.getDeadlineDate());
            dto.setReturnDate(loan.getReturnDate());
            dto.setStatus(loan.getStatus());
            dto.setClientId(loan.getClientId());
            dto.setToolId(loan.getToolId());

            // --- BUSCAR NOMBRE DEL CLIENTE ---
            try {
                // Llamada al microservicio de clientes
                ClientDTO client = restTemplate.getForObject(
                    "http://customer-service/api/clients/" + loan.getClientId(), 
                    ClientDTO.class
                );
                dto.setClientName(client != null ? client.getName() : "Desconocido");
            } catch (Exception e) {
                dto.setClientName("Error al cargar cliente");
            }

            // --- BUSCAR NOMBRE DE LA HERRAMIENTA ---
            try {
                // Llamada al microservicio de inventario
                ToolDTO tool = restTemplate.getForObject(
                    "http://inventory-service/api/tools/" + loan.getToolId(), 
                    ToolDTO.class
                );
                dto.setToolName(tool != null ? tool.getName() : "Desconocido");
            } catch (Exception e) {
                dto.setToolName("Error al cargar herramienta");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}