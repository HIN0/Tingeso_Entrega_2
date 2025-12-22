package tgs.loan_service.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.models.ClientDTO;
import tgs.loan_service.models.ToolDTO;
import tgs.loan_service.repositories.LoanRepository;

import java.time.LocalDate;
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
        // 1. Validar Cliente (Comunicación con M3 - Customer Service)
        // OJO: Asume que M3 se llama "customer-service" en Eureka
        ClientDTO client = restTemplate.getForObject("http://customer-service/api/clients/" + clientId, ClientDTO.class);
        
        if (client == null) throw new RuntimeException("Cliente no encontrado");
        if ("RESTRINGIDO".equals(client.getStatus())) throw new RuntimeException("Cliente no habilitado para préstamos");

        // 2. Validar reglas de préstamos del cliente (Local)
        List<LoanEntity> activeLoans = loanRepository.findByClientIdAndStatus(clientId, "ACTIVE");
        if (activeLoans.size() >= 5) throw new RuntimeException("Cliente excede máximo de 5 préstamos");
        
        if (loanRepository.existsByClientIdAndToolIdAndStatus(clientId, toolId, "ACTIVE")) {
            throw new RuntimeException("Cliente ya tiene esta herramienta en préstamo");
        }

        // 3. Validar Herramienta (Comunicación con M1 - Inventory Service)
        ToolDTO tool = restTemplate.getForObject("http://inventory-service/api/tools/" + toolId, ToolDTO.class);
        
        if (tool == null) throw new RuntimeException("Herramienta no encontrada");
        if (tool.getStock() <= 0) throw new RuntimeException("Herramienta sin stock disponible");
        if (!"AVAILABLE".equals(tool.getStatus())) throw new RuntimeException("Herramienta no disponible para préstamo");

        // 4. Crear el Préstamo (Local)
        LoanEntity newLoan = LoanEntity.builder()
                .loanDate(LocalDate.now())
                .deadlineDate(LocalDate.now().plusDays(7)) // Ejemplo: 7 días por defecto
                .status("ACTIVE")
                .clientId(clientId)
                .toolId(toolId)
                .build();
        
        LoanEntity savedLoan = loanRepository.save(newLoan);

        // 5. Actualizar Stock en M1 (Comunicación con M1)
        // Llamamos al endpoint que creamos en el paso anterior para restar stock (-1)
        // Esto a su vez disparará el Kardex allá en M1.
        try {
            restTemplate.put("http://inventory-service/api/tools/" + toolId + "/stock?quantity=-1", null);
        } catch (Exception e) {
            // Rollback manual sería ideal aquí, pero por simplicidad académica lanzamos error
            throw new RuntimeException("Error actualizando stock en inventario: " + e.getMessage());
        }

        return savedLoan;
    }
}