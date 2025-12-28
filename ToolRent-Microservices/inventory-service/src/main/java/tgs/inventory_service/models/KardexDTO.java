package tgs.inventory_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexDTO {
    private String movementType;
    private Long toolId;
    private int quantity;
    private String username;
}