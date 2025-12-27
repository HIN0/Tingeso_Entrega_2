package tgs.loan_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.models.ClientDTO;
import tgs.loan_service.models.ToolDTO;
import tgs.loan_service.models.TariffDTO;
import tgs.loan_service.repositories.LoanRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    // USO DE CONSTANTES: Facilita cambiar URLs si te mueves a Kubernetes o cambian los puertos
    private final String INVENTORY_URL = "http://inventory-service/api/tools";
    private final String CUSTOMER_URL = "http://customer-service/api/clients";
    private final String KARDEX_URL = "http://kardex-service/api/kardex";
    private final String TARIFF_URL = "http://tariff-service/api/tariffs";

    public List<LoanEntity> getAllLoans() { return loanRepository.findAll(); }
    
    public List<LoanEntity> getLoansByClient(Long clientId) {
        return loanRepository.findByClientIdAndStatus(clientId, "ACTIVE");
    }

    // MEJORA: Método privado para registrar en Kardex correctamente
    private void registerKardex(Long toolId, String type, int quantity, String username) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("toolId", toolId);
            // CORRECCIÓN CRÍTICA: El DTO espera "movementType", no "type"
            request.put("movementType", type); 
            request.put("quantity", quantity);
            request.put("username", username);
            // La fecha la puede poner el microservicio destino, pero si la envías, asegúrate que el DTO la reciba.
            // Si KardexDTO no tiene campo fecha, no hace falta enviarla aquí.
            
            restTemplate.postForObject(KARDEX_URL, request, Void.class);
        } catch (Exception e) {
            System.err.println("Error reportando a Kardex: " + e.getMessage());
        }
    }

    public LoanEntity createLoan(Long clientId, Long toolId, String username) {
        // 1. VALIDACIONES
        ClientDTO client = restTemplate.getForObject(CUSTOMER_URL + "/" + clientId, ClientDTO.class);
        if (client == null) throw new RuntimeException("Cliente no existe");
        
        // Validación de Estado (RESTRINGIDO por Deuda)
        if ("RESTRINGIDO".equals(client.getStatus())) {
            throw new RuntimeException("Cliente no habilitado (RESTRINGIDO). Revise deudas.");
        }

        List<LoanEntity> activeLoans = loanRepository.findByClientIdAndStatus(clientId, "ACTIVE");
        if (activeLoans.size() >= 5) throw new RuntimeException("Cliente excede máximo de 5 préstamos activos");
        
        if (loanRepository.existsByClientIdAndToolIdAndStatus(clientId, toolId, "ACTIVE")) {
            throw new RuntimeException("Cliente ya tiene esta herramienta prestada");
        }

        ToolDTO tool = restTemplate.getForObject(INVENTORY_URL + "/" + toolId, ToolDTO.class);
        if (tool == null || tool.getStock() <= 0) throw new RuntimeException("Herramienta sin stock.");

        // 2. EJECUCIÓN CON SEGURIDAD (SAGA PATTERN)
        
        // Paso A: Descontar Stock (Llamada externa)
        try {
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=-1&username=" + username, null);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Inventario: " + e.getMessage());
        }

        // Paso B: Guardar Préstamo Localmente
        LoanEntity newLoan = LoanEntity.builder()
                .loanDate(LocalDate.now())
                .deadlineDate(LocalDate.now().plusDays(7)) 
                .status("ACTIVE")
                .clientId(clientId)
                .toolId(toolId)
                .build();

        LoanEntity savedLoan;
        try {
            savedLoan = loanRepository.save(newLoan);
        } catch (Exception e) {
            // MEJORA CRÍTICA: ROLLBACK
            // Si falló guardar en MI base de datos, debo devolver el stock al inventario
            // para no generar inconsistencia (herramienta perdida).
            System.err.println("Fallo al guardar préstamo local. Reviertiendo stock...");
            restTemplate.put(INVENTORY_URL + "/" + toolId + "/stock?quantity=1&username=" + username, null);
            throw new RuntimeException("Error interno al procesar el préstamo. Intente nuevamente.");
        }

        // Paso C: Registrar en Kardex
        registerKardex(toolId, "PRESTAMO", 1, username); // Envia cantidad positiva, el tipo define si resta

        return savedLoan;
    }

    public LoanEntity returnLoan(Long loanId, String username) {
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!"ACTIVE".equals(loan.getStatus())) throw new RuntimeException("Préstamo ya cerrado");

        // 1. Cálculos
        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        TariffDTO tariff;
        try {
            tariff = restTemplate.getForObject(TARIFF_URL, TariffDTO.class);
        } catch (Exception e) {
            // Fallback seguro si falla tarifa
            tariff = new TariffDTO(); 
            tariff.setDailyRentFee(1000); 
            tariff.setDailyLateFee(2000);
        }

        long daysRented = ChronoUnit.DAYS.between(loan.getLoanDate(), returnDate);
        if (daysRented < 1) daysRented = 1;

        long daysOverdue = 0;
        if (returnDate.isAfter(loan.getDeadlineDate())) {
            daysOverdue = ChronoUnit.DAYS.between(loan.getDeadlineDate(), returnDate);
        }

        double totalToPay = (daysRented * tariff.getDailyRentFee()) + (daysOverdue * tariff.getDailyLateFee());

        // 2. Registrar Deuda en Cliente
        if (totalToPay > 0) {
            try {
                restTemplate.put(CUSTOMER_URL + "/" + loan.getClientId() + "/balance?amount=" + totalToPay, null);
            } catch (Exception e) {
                System.err.println("Error actualizando saldo cliente: " + e.getMessage());
            }
        }

        // 3. Devolver Stock
        try {
            restTemplate.put(INVENTORY_URL + "/" + loan.getToolId() + "/stock?quantity=1&username=" + username, null);
        } catch (Exception e) {
             // Aquí es más complejo hacer rollback porque ya cobraste deuda.
             // Se asume eventual consistency o alerta manual.
            System.err.println("Error crítico devolviendo stock: " + e.getMessage());
        }

        // 4. Cerrar Préstamo
        loan.setStatus("RETURNED");
        LoanEntity savedLoan = loanRepository.save(loan);

        // 5. Kardex
        registerKardex(loan.getToolId(), "DEVOLUCION", 1, username);

        return savedLoan;
    }
}