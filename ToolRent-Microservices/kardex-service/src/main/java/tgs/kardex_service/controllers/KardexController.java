package tgs.kardex_service.controllers;

import tgs.kardex_service.entities.KardexEntity;
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
    public ResponseEntity<?> createEntry(@RequestBody KardexEntity entry) {
        try {
            // RECTIFICACIÓN: Asegurar valores mínimos antes de guardar
            if (entry.getDate() == null) {
                entry.setDate(LocalDate.now());
            }
            // Evitar que JPA falle por campos nulos opcionales
            if (entry.getStockAfter() == 0) {
                entry.setStockAfter(0); 
            }
            
            KardexEntity saved = kardexService.saveEntry(entry);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar JSON: " + e.getMessage());
        }
    }
}