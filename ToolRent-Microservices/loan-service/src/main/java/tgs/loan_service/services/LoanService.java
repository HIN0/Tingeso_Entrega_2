package tgs.loan_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.models.ClientDTO;
import tgs.loan_service.models.ToolDTO;
import tgs.loan_service.models.TariffDTO;
import tgs.loan_service.repositories.LoanRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Métodos de consulta
    public List<LoanEntity> getAllLoans() { return loanRepository.findAll(); }
    
    public List<LoanEntity> getLoansByClient(Long clientId) {
        return loanRepository.findByClientIdAndStatus(clientId, "ACTIVE");
    }

    public LoanEntity createLoan(Long clientId, Long toolId, String username) {
        // ----------------------------------------------------------------
        // 1. VALIDACIONES PREVIAS (LECTURA)
        // ----------------------------------------------------------------

        // 1.1 Validar Cliente (Comunicación con M3 - Customer Service)
        ClientDTO client = null;
        try {
            client = restTemplate.getForObject("http://customer-service/api/clients/" + clientId, ClientDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Servicio de Clientes o cliente no existe.");
        }
        
        if (client == null) throw new RuntimeException("Cliente no encontrado");
        if ("RESTRINGIDO".equals(client.getStatus())) throw new RuntimeException("Cliente no habilitado para préstamos (RESTRINGIDO)");

        // 1.2 Validar reglas de negocio locales del cliente
        List<LoanEntity> activeLoans = loanRepository.findByClientIdAndStatus(clientId, "ACTIVE");
        if (activeLoans.size() >= 5) throw new RuntimeException("Cliente excede máximo de 5 préstamos activos");
        
        if (loanRepository.existsByClientIdAndToolIdAndStatus(clientId, toolId, "ACTIVE")) {
            throw new RuntimeException("Cliente ya tiene esta herramienta en préstamo actualmente");
        }

        // 1.3 Validar Herramienta (Comunicación con M1 - Inventory Service)
        ToolDTO tool = null;
        try {
            tool = restTemplate.getForObject("http://inventory-service/api/tools/" + toolId, ToolDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Servicio de Inventario o herramienta no existe.");
        }
        
        if (tool == null) throw new RuntimeException("Herramienta no encontrada");
        if (tool.getStock() <= 0) throw new RuntimeException("Herramienta sin stock disponible");
        // Aseguramos que solo se preste si está disponible (o disponible con stock)
        if (!"AVAILABLE".equals(tool.getStatus())) throw new RuntimeException("Herramienta no disponible para préstamo (Estado: " + tool.getStatus() + ")");


        // ----------------------------------------------------------------
        // 2. EJECUCIÓN CON COMPENSACIÓN (SAGA PATTERN)
        // ----------------------------------------------------------------
        
        // PASO A: Intentar descontar stock en Inventario (Llamada Externa)
        // Si esto falla, lanzamos error y NO guardamos nada en nuestra BD.
        try {
            // Nota: Se agrega 'username' en la query string si el endpoint de Inventory lo soporta, para trazabilidad en Kardex
            restTemplate.put("http://inventory-service/api/tools/" + toolId + "/stock?quantity=-1&username=" + username, null);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al descontar stock en Inventario: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión al intentar descontar stock: " + e.getMessage());
        }

        // PASO B: Guardar el Préstamo en BD Local
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
            // !!! CRÍTICO: Si falla el guardado local, debemos COMPENSAR (deshacer) el cambio en inventario !!!
            System.err.println("Error guardando préstamo localmente. Iniciando Rollback en Inventario...");
            try {
                // Rollback: Sumamos 1 al stock para dejarlo como estaba
                restTemplate.put("http://inventory-service/api/tools/" + toolId + "/stock?quantity=1&username=" + username, null);
            } catch (Exception rollbackEx) {
                // Si falla el rollback, estamos en problemas graves (Inconsistencia de datos).
                // En producción esto se enviaría a una cola de errores o log de alertas críticas.
                System.err.println("FATAL: Falló el rollback de stock para herramienta " + toolId + ". Inconsistencia detectada.");
            }
            throw new RuntimeException("Error interno al crear el préstamo. Se ha revertido el stock.");
        }
        return savedLoan;
    }

    public LoanEntity returnLoan(Long loanId, String username) {
        // 1. Obtener Préstamo
        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new RuntimeException("El préstamo no está activo (Estado actual: " + loan.getStatus() + ")");
        }

        // 2. Calcular Fechas y Multas (RF2.4)
        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        // Obtener tarifas actuales (Comunicación con M4)
        TariffDTO tariff;
        try {
            tariff = restTemplate.getForObject("http://tariff-service/api/tariffs", TariffDTO.class);
        } catch (Exception e) {
            // Si falla el servicio de tarifas, usamos valores por defecto o lanzamos error.
            // Para seguridad del negocio, mejor lanzar error.
            throw new RuntimeException("Error obteniendo tarifas para calcular pagos.");
        }

        // Cálculos de costos (Para mostrar al frontend o guardar histórico si existiera campo)
        long daysRented = ChronoUnit.DAYS.between(loan.getLoanDate(), returnDate);
        if (daysRented < 1) daysRented = 1; // Cobro mínimo 1 día

        long daysOverdue = 0;
        if (returnDate.isAfter(loan.getDeadlineDate())) {
            daysOverdue = ChronoUnit.DAYS.between(loan.getDeadlineDate(), returnDate);
        }

        double rentCost = daysRented * tariff.getDailyRentFee();
        double lateFee = daysOverdue * tariff.getDailyLateFee();
        double totalToPay = rentCost + lateFee;

        // NOTA: Aquí podrías guardar 'totalToPay' si agregaras un campo 'paidAmount' a la entidad LoanEntity.
        // Por ahora, seguimos la estructura actual y solo cerramos el préstamo.

        // 3. Actualizar Stock en Inventario (Saga Step 1: External)
        // Sumamos 1 al stock. El microservicio de inventario se encarga de ponerla AVAILABLE.
        try {
            restTemplate.put("http://inventory-service/api/tools/" + loan.getToolId() + "/stock?quantity=1&username=" + username, null);
        } catch (Exception e) {
            throw new RuntimeException("Error al devolver stock al inventario: " + e.getMessage());
        }

        // 4. Guardar Cambios en Préstamo (Saga Step 2: Local)
        loan.setStatus("RETURNED");
        
        LoanEntity savedLoan;
        try {
            savedLoan = loanRepository.save(loan);
        } catch (Exception e) {
            // ROLLBACK MANUAL: Si falla guardar el estado RETURNED, debemos volver a restar el stock
            // para que no quede la herramienta disponible mientras el préstamo sigue activo.
            System.err.println("Error guardando devolución. Iniciando compensación de stock...");
            try {
                restTemplate.put("http://inventory-service/api/tools/" + loan.getToolId() + "/stock?quantity=-1&username=" + username, null);
            } catch (Exception rollbackEx) {
                System.err.println("FATAL: Inconsistencia en devolución préstamo ID " + loanId);
            }
            throw new RuntimeException("Error interno al registrar devolución.");
        }

        return savedLoan; 
        // En una implementación real, aquí devolverías un DTO con el desglose de lo que debe pagar el cliente (rentCost, lateFee).
    }


}