package tgs.kardex_service.controllers;

import tgs.kardex_service.entities.KardexEntity;
import tgs.kardex_service.services.KardexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<KardexEntity> createMovement(@RequestBody KardexEntity kardex) {
        return ResponseEntity.ok(kardexService.registerMovement(kardex));
    }
}