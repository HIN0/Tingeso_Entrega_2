package tgs.kardex_service.controllers;

import tgs.kardex_service.entities.KardexEntity;
import tgs.kardex_service.models.KardexRequest; // Importa el nuevo DTO
import tgs.kardex_service.services.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/kardex")
public class KardexController {

    @Autowired
    private KardexService kardexService;

    @GetMapping
    public ResponseEntity<List<KardexEntity>> getAll() {
        return ResponseEntity.ok(kardexService.getAll());
    }

    @GetMapping("/tool/{toolId}")
    public ResponseEntity<List<KardexEntity>> getByTool(@PathVariable Long toolId) {
        return ResponseEntity.ok(kardexService.getByToolId(toolId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody KardexRequest dto) { 
        try {

            KardexEntity entry = KardexEntity.builder()
                    .movementType(dto.getMovementType())
                    .toolId(dto.getToolId())
                    .quantity(dto.getQuantity())
                    .username(dto.getUsername())
                    .date(LocalDate.now())
                    .stockAfter(0)         
                    .build();
            
            KardexEntity saved = kardexService.saveEntry(entry);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar JSON: " + e.getMessage());
        }
    }
}