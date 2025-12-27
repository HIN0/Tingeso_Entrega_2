package tgs.customer_service.repositories;

import tgs.customer_service.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    ClientEntity findByRut(String rut);
    
    // Buscar clientes por estado (RF6.2)
    List<ClientEntity> findByStatus(String status);
    
    // Buscar clientes con deuda mayor a X (Mejora de rendimiento)
    List<ClientEntity> findByBalanceGreaterThan(Double balance);
}