package tgs.loan_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KardexDTO {
    private String movementType;
    private Long toolId;
    private Integer quantity;
    private String username;
}