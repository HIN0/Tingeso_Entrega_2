package tgs.inventory_service.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tgs.inventory_service.entities.ToolEntity;
import tgs.inventory_service.services.ToolService;

import java.util.List;

@RestController
@RequestMapping("/api/tools") // Ojo con el prefijo /api
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
        if (tool == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tool);
    }

    @PostMapping
    public ResponseEntity<ToolEntity> createTool(@RequestBody ToolEntity tool, 
                                                 @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        // Nota: En microservicios, el Gateway suele pasar el usuario en un Header.
        // Por ahora simulamos que viene en "X-User-Name" o por defecto "admin".
        return ResponseEntity.ok(toolService.createTool(tool, username));
    }
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<ToolEntity> updateStock(@PathVariable Long id, 
                                                  @RequestParam int quantity,
                                                  @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        return ResponseEntity.ok(toolService.updateStock(id, quantity, username));
    }
}