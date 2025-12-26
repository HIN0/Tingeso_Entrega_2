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

    // URL del microservicio M5 (Kardex). Usamos el nombre del servicio registrado en Eureka.
    // NOTA: Asegúrate de que M5 se llame 'kardex-service' en su application.properties
    private final String KARDEX_SERVICE_URL = "http://kardex-service/api/kardex";

    public List<ToolEntity> getAllTools() {
        return toolRepository.findAll();
    }

    public ToolEntity getToolById(Long id) {
        return toolRepository.findById(id).orElse(null);
    }

    @Transactional
    public ToolEntity createTool(ToolEntity tool, String username) {
        if (tool.getInRepair() == null) tool.setInRepair(0);
        
        // Lógica de stock inicial
        if (tool.getStatus() == null) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (tool.getStock() > 0 && tool.getStatus() != ToolStatus.AVAILABLE) {
            tool.setStatus(ToolStatus.AVAILABLE);
        }

        ToolEntity saved = toolRepository.save(tool);

        // Comunicación con M5 (Kardex) vía HTTP
        if (saved.getStock() > 0) {
            KardexDTO kardexRequest = new KardexDTO("INCOME", saved.getId(), saved.getStock(), username);
            // Enviamos la petición POST a M5. Usamos try-catch para evitar que falle si M5 está caído.
            try {
                restTemplate.postForObject(KARDEX_SERVICE_URL, kardexRequest, Void.class);
            } catch (Exception e) {
                throw new RuntimeException("Error crítico: No se pudo registrar en Kardex. Operación cancelada.");
            }
        }
        return saved;
    }

    @Transactional
    public ToolEntity updateStock(Long id, int quantity, String username) {
        ToolEntity tool = getToolById(id);
        if (tool == null) return null;

        int newStock = tool.getStock() + quantity;
        if (newStock < 0) throw new RuntimeException("Stock cannot be negative");

        tool.setStock(newStock);
        if (newStock > 0 && tool.getStatus() != ToolStatus.REPAIRING) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (newStock == 0) {
            tool.setStatus(ToolStatus.LOANED); // O el estado que corresponda según lógica
        }
        
        ToolEntity saved = toolRepository.save(tool);
        
        // Registrar en Kardex (M5)
        String type = quantity > 0 ? "INCOME" : "MANUAL_DECREASE";
        KardexDTO kardexRequest = new KardexDTO(type, saved.getId(), Math.abs(quantity), username);
        try {
            restTemplate.postForObject(KARDEX_SERVICE_URL, kardexRequest, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Error crítico: No se pudo registrar en Kardex. Operación cancelada.");
        }

        return saved;
    }
}