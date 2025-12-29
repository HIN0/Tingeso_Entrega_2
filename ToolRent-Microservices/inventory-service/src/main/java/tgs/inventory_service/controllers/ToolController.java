package tgs.inventory_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgs.inventory_service.entities.ToolEntity;
import tgs.inventory_service.services.ToolService;
import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<List<ToolEntity>> getAllTools() {
        return ResponseEntity.ok(toolService.getAllTools());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolEntity> getToolById(@PathVariable Long id) {
        ToolEntity tool = toolService.getToolById(id);
        return (tool == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(tool);
    }

    @PostMapping
    public ResponseEntity<ToolEntity> createTool(@RequestBody ToolEntity tool, 
                                                 @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        return ResponseEntity.ok(toolService.createTool(tool, username));
    }
    
    // endpoint existente para stock
    @PutMapping("/{id}/stock")
    public ResponseEntity<ToolEntity> updateStock(@PathVariable Long id, 
                                                  @RequestParam int quantity,
                                                  @RequestParam(defaultValue = "false") boolean skipKardex,
                                                  @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        return ResponseEntity.ok(toolService.updateStock(id, quantity, username, skipKardex));
    }

    // 1. Endpoint para EDITAR (PUT /api/tools/{id})
    @PutMapping("/{id}")
    public ResponseEntity<ToolEntity> updateTool(@PathVariable Long id, @RequestBody ToolEntity tool) {
        ToolEntity updated = toolService.updateTool(id, tool);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // 2. Endpoint para CAMBIAR ESTADO  (PUT /api/tools/{id}/status)
    @PutMapping("/{id}/status")
    public ResponseEntity<ToolEntity> updateStatus(
            @PathVariable Long id, 
            @RequestParam String newStatus, // Recibe ?newStatus=DECOMMISSIONED
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        
        try {
            ToolEntity updatedTool = toolService.changeStatus(id, newStatus, username);
            if (updatedTool == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updatedTool);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint auxiliar para reparación (si lo usas)
    @PutMapping("/{id}/repair")
    public ResponseEntity<ToolEntity> markForRepair(@PathVariable Long id) {
        ToolEntity updatedTool = toolService.incrementInRepair(id);
        return (updatedTool != null) ? ResponseEntity.ok(updatedTool) : ResponseEntity.notFound().build();
    }
}