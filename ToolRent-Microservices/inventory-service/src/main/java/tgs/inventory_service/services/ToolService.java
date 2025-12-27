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

    // Asegúrate de que este nombre coincida con el registrado en Eureka (o usa localhost:8080 si pruebas local sin gateway)
    private final String KARDEX_SERVICE_URL = "http://kardex-service/api/kardex";

    public List<ToolEntity> getAllTools() {
        return toolRepository.findAll();
    }

    public ToolEntity getToolById(Long id) {
        return toolRepository.findById(id).orElse(null);
    }

    @Transactional
    public ToolEntity createTool(ToolEntity tool, String username) {
        // Validación de valores nulos para evitar NullPointerException
        if (tool.getInRepair() == null) tool.setInRepair(0);
        
        // 2. Usar ToolStatus.AVAILABLE (Enum)
        if (tool.getStatus() == null) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } else if (tool.getStock() > 0 && tool.getStatus() != ToolStatus.AVAILABLE) {
            // Si hay stock, forzamos a que esté disponible
            tool.setStatus(ToolStatus.AVAILABLE);
        }

        ToolEntity saved = toolRepository.save(tool);

        // Integración con Kardex
        if (saved.getStock() > 0) {
            // Asumiendo que KardexDTO acepta Strings para el tipo de movimiento.
            KardexDTO kardexRequest = new KardexDTO("INCOME", saved.getId(), saved.getStock(), username);
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
        if (newStock < 0) throw new RuntimeException("El stock no puede ser negativo");

        tool.setStock(newStock);

        // 3. CORRECCIÓN: Lógica de estados usando Enums
        // Si el stock sube y no estaba en reparación, pasa a disponible
        if (newStock > 0 && tool.getStatus() != ToolStatus.REPAIRING) {
            tool.setStatus(ToolStatus.AVAILABLE);
        } 
        // Si el stock llega a 0, técnicamente se prestó (si quantity < 0)
        else if (newStock == 0) {
            // Aquí asumo que si el stock baja a 0 es porque se prestó.
            // Si el enum tiene "LOANED", lo usamos.
            tool.setStatus(ToolStatus.LOANED);
        }
        
        ToolEntity saved = toolRepository.save(tool);
        
        // Integración con Kardex (M5)
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