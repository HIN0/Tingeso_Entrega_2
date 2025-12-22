package tgs.loan_service.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.services.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
    
    // Endpoint para solicitar un préstamo
    @PostMapping
    public ResponseEntity<?> createLoan(@RequestParam Long clientId, 
                                        @RequestParam Long toolId,
                                        @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        try {
            LoanEntity loan = loanService.createLoan(clientId, toolId, username);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}