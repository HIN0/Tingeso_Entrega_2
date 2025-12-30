package tgs.kardex_service.services;

import tgs.kardex_service.entities.KardexEntity;
import tgs.kardex_service.repositories.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    public List<KardexEntity> getAll() {
        return kardexRepository.findAll();
    }
    
    public List<KardexEntity> getByToolId(Long toolId) {
        return kardexRepository.findByToolId(toolId);
    }

    public KardexEntity registerMovement(KardexEntity kardex) {
        if (kardex.getDate() == null) {
            kardex.setDate(LocalDate.now());
        }
        return kardexRepository.save(kardex);
    }
    public KardexEntity saveEntry(KardexEntity entry) {
        return kardexRepository.save(entry);
    }

    public List<KardexEntity> getKardex(Long toolId, LocalDate startDate, LocalDate endDate) {
        // Caso 1: Filtro completo (ID y Fechas)
        if (toolId != null && startDate != null && endDate != null) {
            return kardexRepository.findByToolIdAndDateBetween(toolId, startDate, endDate);
        }
        // Caso 2: Solo fechas (RF5.3)
        else if (startDate != null && endDate != null) {
            return kardexRepository.findByDateBetween(startDate, endDate);
        }
        // Caso 3: Solo ID herramienta (RF5.2)
        else if (toolId != null) {
            return kardexRepository.findByToolId(toolId);
        }
        // Caso 4: Sin filtros (Traer todo)
        else {
            return kardexRepository.findAll();
        }
    }
}