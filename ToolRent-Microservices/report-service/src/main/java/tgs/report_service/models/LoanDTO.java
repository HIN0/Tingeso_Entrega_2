package tgs.report_service.models;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoanDTO {
    private Long id;
    private LocalDate loanDate;
    private LocalDate deadlineDate;
    private LocalDate returnDate;
    private String status; // ACTIVE, RETURNED, OVERDUE
    private Long toolId;
    private Long clientId;
}