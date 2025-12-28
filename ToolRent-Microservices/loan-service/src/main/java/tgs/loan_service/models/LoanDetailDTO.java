package tgs.loan_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDetailDTO {
    private Long id;
    private LocalDate loanDate;
    private LocalDate deadlineDate;
    private LocalDate returnDate;
    private String status;
    
    private Long clientId;
    private String clientName; // "Juan Perez"
    
    private Long toolId;
    private String toolName;   // "Taladro Percutor"
}