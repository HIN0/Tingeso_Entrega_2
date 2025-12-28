package tgs.loan_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tgs.loan_service.entitites.LoanEntity;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    
    // Regla de 5 préstamos
    List<LoanEntity> findByClientIdAndStatus(Long clientId, String status);

    // NUEVO: Validar unicidad (Evita que el cliente tenga la misma herramienta 2 veces)
    boolean existsByClientIdAndToolIdAndStatus(Long clientId, Long toolId, String status);

    // NUEVO: Optimización para reportes (Solo trae activos desde la BD)
    @Query("SELECT l FROM LoanEntity l WHERE l.status = :status")
    List<LoanEntity> findAllByStatus(@Param("status") String status);
}