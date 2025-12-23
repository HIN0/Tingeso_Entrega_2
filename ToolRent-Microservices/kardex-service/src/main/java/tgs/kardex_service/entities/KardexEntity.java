package tgs.kardex_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String movementType; // "INCOME", "LOAN", "RETURN", "REPAIR", "DECOMMISSION"
    private LocalDate date;
    private int quantity;
    private int stockAfter; // Opcional: Stock resultante tras el movimiento
    
    // Referencias a otros microservicios
    private Long toolId;     // ID de la herramienta (M1)
    private String username; // Usuario que hizo la acción (M7/Security)
}