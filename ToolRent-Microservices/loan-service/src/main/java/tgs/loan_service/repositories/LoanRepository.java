package tgs.loan_service.repositories;

import tgs.loan_service.entitites.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    // Buscar préstamos activos de un cliente
    List<LoanEntity> findByClientIdAndStatus(Long clientId, String status);
    
    // Buscar si un cliente ya tiene una herramienta específica prestada
    boolean existsByClientIdAndToolIdAndStatus(Long clientId, Long toolId, String status);
}