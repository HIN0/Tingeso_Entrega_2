package tgs.inventory_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KardexRequestDTO {
    private Long toolId;
    private String movementType;
    private int quantity;
    private String username;
}