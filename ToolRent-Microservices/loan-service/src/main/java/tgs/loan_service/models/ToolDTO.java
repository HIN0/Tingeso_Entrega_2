package tgs.loan_service.models;

import lombok.Data;

@Data
public class ToolDTO {
    private Long id;
    private String name;
    private String category;
    private String status;
    private Integer stock;
    private Integer inRepair;
    private Integer replacementValue;
}