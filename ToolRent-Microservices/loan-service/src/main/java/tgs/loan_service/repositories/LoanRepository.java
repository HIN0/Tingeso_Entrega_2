package tgs.loan_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tgs.loan_service.entitites.LoanEntity;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findByClientIdAndStatus(Long clientId, String status);
    
    // Para el Reporte de Préstamos Activos (RF6.1)
    List<LoanEntity> findByStatus(String status);
    
    // Verifica si existe préstamo activo para evitar duplicados
    boolean existsByClientIdAndToolIdAndStatus(Long clientId, Long toolId, String status);
}