package tgs.kardex_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tgs.kardex_service.entities.KardexEntity;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    
    // RF5.2: Buscar por herramienta
    List<KardexEntity> findByToolId(Long toolId);
    
    // RF5.3: Buscar por rango de fechas
    List<KardexEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Extra: Buscar por herramienta Y rango de fechas (Combinado)
    List<KardexEntity> findByToolIdAndDateBetween(Long toolId, LocalDate startDate, LocalDate endDate);
}