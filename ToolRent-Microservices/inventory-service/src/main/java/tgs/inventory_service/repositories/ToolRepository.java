package tgs.inventory_service.repositories;

import tgs.inventory_service.entities.ToolEntity;
import tgs.inventory_service.entities.ToolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<ToolEntity, Long> {
    List<ToolEntity> findByStatus(ToolStatus status);
    List<ToolEntity> findByNameContainingIgnoreCase(String name);
}