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
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<ToolEntity> updateStock
                (@PathVariable Long id, 
                @RequestParam int quantity,
                @RequestParam(defaultValue = "false") boolean skipKardex,
                @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        return ResponseEntity.ok(toolService.updateStock(id, quantity, username, skipKardex));
    }

    @PutMapping("/{id}/repair")
        public ResponseEntity<ToolEntity> markForRepair(
                @PathVariable Long id, 
                @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
            
            ToolEntity updatedTool = toolService.incrementInRepair(id);
            if (updatedTool == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedTool);
        }

}