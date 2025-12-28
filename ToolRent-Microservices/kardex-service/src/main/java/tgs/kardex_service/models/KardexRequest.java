package tgs.kardex_service.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexRequest {
    private String movementType;
    private Long toolId;
    private int quantity;
    private String username;
}