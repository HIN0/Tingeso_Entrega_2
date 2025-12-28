package tgs.loan_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.models.LoanDetailDTO;
import tgs.loan_service.models.ReturnLoanDTO;
import tgs.loan_service.services.LoanService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanDetailDTO>> getAllLoans() {
        List<LoanDetailDTO> loans = loanService.findAllWithDetails();
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }
    
    // NUEVO: Endpoint optimizado
    @GetMapping("/active")
    public ResponseEntity<List<LoanEntity>> getActiveLoans() {
        return ResponseEntity.ok(loanService.getActiveLoans());
    }

    // MODIFICADO: Recibe fecha opcional
    @PostMapping
    public ResponseEntity<LoanEntity> createLoan(
            @RequestParam Long clientId, 
            @RequestParam Long toolId, 
            @RequestParam(required = false) String deadline,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        
        LocalDate deadlineDate = null;
        if (deadline != null && !deadline.isEmpty()) {
            try {
                deadlineDate = LocalDate.parse(deadline);
            } catch (Exception e) {
            }
        }
        return ResponseEntity.ok(loanService.createLoan(clientId, toolId, username, deadlineDate));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<LoanEntity> returnLoan(
            @PathVariable Long id,
            @RequestBody ReturnLoanDTO dto,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        
        String condition = "GOOD";
        if (Boolean.TRUE.equals(dto.getDamaged())) {
            condition = "DAMAGED";
            if (Boolean.TRUE.equals(dto.getIrreparable())) {
                condition = "DESTROYED";
            }
        }
        LoanEntity loan = loanService.returnLoan(id, username, condition);
        return ResponseEntity.ok(loan);
    }
}