package tgs.tariff_service.controllers;

import tgs.tariff_service.entities.TariffEntity;
import tgs.tariff_service.services.TariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {

    @Autowired
    private TariffService tariffService;

    @GetMapping
    public ResponseEntity<TariffEntity> getTariff() {
        return ResponseEntity.ok(tariffService.getCurrentTariff());
    }

    @PostMapping // O PUT, pero usaremos POST para simplificar la actualización única
    public ResponseEntity<TariffEntity> updateTariff(@RequestBody TariffEntity tariff) {
        return ResponseEntity.ok(tariffService.updateTariff(tariff));
    }
}