package tgs.loan_service.entitites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate loanDate;      // Fecha inicio préstamo (startDate)
    private LocalDate deadlineDate;  // Fecha pactada devolución
    private LocalDate returnDate;    // Fecha real devolución (puede ser null)
    
    private String status;

    private Long clientId;
    private Long toolId;
}