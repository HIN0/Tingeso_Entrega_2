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
}