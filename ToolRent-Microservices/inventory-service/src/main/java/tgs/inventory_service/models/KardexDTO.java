package tgs.inventory_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KardexDTO {
    private String movementType; // "INCOME", "DECOMMISSION", etc.
    private Long toolId;
    private int quantity;
    private String username; // Enviamos el nombre del usuario, no la entidad completa
}