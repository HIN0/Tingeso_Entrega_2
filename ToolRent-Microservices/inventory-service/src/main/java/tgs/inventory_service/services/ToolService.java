package tgs.inventory_service.services;

import tgs.inventory_service.entities.ToolEntity;
import tgs.inventory_service.entities.ToolStatus;
import tgs.inventory_service.models.KardexDTO;
import tgs.inventory_service.repositories.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String KARDEX_SERVICE_URL = "http://kardex-service/api/kardex";

    public List<ToolEntity> getAllTools() { return toolRepository.findAll(); }

    public ToolEntity getToolById(Long id) { return toolRepository.findById(id).orElse(null); }

    @Transactional
    public ToolEntity createTool(ToolEntity tool, String username) {
        if (tool.getInRepair() == null) tool.setInRepair(0);
        if (tool.getStatus() == null) tool.setStatus(ToolStatus.AVAILABLE);

        ToolEntity saved = toolRepository.save(tool);

        if (saved.getStock() > 0) {
            reportKardex("INCOME", saved.getId(), saved.getStock(), username);
        }
        return saved;
    }

    @Transactional
    public ToolEntity updateStock(Long id, int quantity, String username) {
        ToolEntity tool = getToolById(id);
        if (tool == null) throw new RuntimeException("Herramienta no encontrada");

        int newStock = tool.getStock() + quantity;
        if (newStock < 0) throw new RuntimeException("Stock insuficiente");

        tool.setStock(newStock);

        // Lógica automática de estados basada en stock
        if (newStock > 0 && tool.getStatus() != ToolStatus.REPAIRING && tool.getStatus() != ToolStatus.DECOMMISSIONED) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (newStock == 0 && quantity < 0) {
            tool.setStatus(ToolStatus.LOANED);
        }

        ToolEntity saved = toolRepository.save(tool);
        String type = quantity > 0 ? "RETURN_STOCK" : "LOAN_OUT"; // Más descriptivo
        reportKardex(type, saved.getId(), Math.abs(quantity), username);

        return saved;
    }

    // NUEVO MÉTODO: Para manejar Daños y Bajas (RF1.2)
    @Transactional
    public ToolEntity changeStatus(Long id, String newStatusStr, String username) {
        ToolEntity tool = getToolById(id);
        if (tool == null) return null;

        ToolStatus newStatus;
        try {
            newStatus = ToolStatus.valueOf(newStatusStr); // Espera "REPAIRING", "DECOMMISSIONED", "AVAILABLE"
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + newStatusStr);
        }

        tool.setStatus(newStatus);
        ToolEntity saved = toolRepository.save(tool);

        // Registrar en Kardex el cambio de estado (ej. BAJA)
        reportKardex("STATUS_CHANGE_" + newStatusStr, saved.getId(), 0, username);
        return saved;
    }

    private void reportKardex(String type, Long toolId, int quantity, String username) {
        try {
            KardexDTO kardexRequest = new KardexDTO(type, toolId, quantity, username);
            restTemplate.postForObject(KARDEX_SERVICE_URL, kardexRequest, Void.class);
        } catch (Exception e) {
            System.err.println("Error reportando a Kardex: " + e.getMessage());
        }
    }
}