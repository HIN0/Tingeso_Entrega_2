package tgs.loan_service.models;

import lombok.Data;

@Data
public class ToolDTO {
    private Long id;
    private String name;
    private String status; // AVAILABLE, LOANED, etc.
    private Integer stock;
}