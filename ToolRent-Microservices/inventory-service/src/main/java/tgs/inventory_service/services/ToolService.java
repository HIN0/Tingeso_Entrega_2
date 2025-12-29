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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RestTemplate restTemplate;

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
    public ToolEntity updateStock(Long id, int quantity, String username, boolean skipKardex) {
        ToolEntity tool = getToolById(id);
        if (tool == null) throw new RuntimeException("Herramienta no encontrada");

        int newStock = tool.getStock() + quantity;
        if (newStock < 0) throw new RuntimeException("Stock insuficiente");

        tool.setStock(newStock);

        if (newStock > 0 && tool.getStatus() != ToolStatus.REPAIRING && tool.getStatus() != ToolStatus.DECOMMISSIONED) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (newStock == 0 && quantity < 0) {
            tool.setStatus(ToolStatus.LOANED);
        }

        ToolEntity saved = toolRepository.save(tool);
        
        // Solo reporta si NO se pide saltar
        if (!skipKardex) {
            String type = quantity > 0 ? "RETURN_STOCK" : "LOAN_OUT";
            reportKardex(type, saved.getId(), Math.abs(quantity), username);
        }

        return saved;
    }

    @Transactional
    public ToolEntity changeStatus(Long id, String newStatusStr, String username) {
        ToolEntity tool = getToolById(id);
        if (tool == null) return null;
        try {
            tool.setStatus(ToolStatus.valueOf(newStatusStr));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido");
        }
        ToolEntity saved = toolRepository.save(tool);
        reportKardex("STATUS_CHANGE_" + newStatusStr, saved.getId(), 0, username);
        return saved;
    }

    private void reportKardex(String type, Long toolId, int quantity, String username) {
        try {
            KardexDTO request = new KardexDTO(type, toolId, Math.abs(quantity), username);
            restTemplate.postForObject("http://kardex-service/api/kardex", request, Void.class);
            log.info("Kardex reportado: {}", type);
        } catch (Exception e) {
            log.error("Error Kardex: {}", e.getMessage());
        }
    }

    public ToolEntity incrementInRepair(Long id) {
        ToolEntity tool = getToolById(id);
        if (tool == null) return null;

        // Incrementamos el contador
        int currentInRepair = (tool.getInRepair() == null) ? 0 : tool.getInRepair();
        tool.setInRepair(currentInRepair + 1);

        return toolRepository.save(tool);
    }
}