package tgs.loan_service.models;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReturnLoanDTO {
    private Long toolId;
    private Boolean damaged;
    private Boolean irreparable;
    private LocalDate returnDate;
}