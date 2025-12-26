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
    
    @PostMapping
        public ResponseEntity<?> createLoan(@RequestParam Long clientId, 
                                            @RequestParam Long toolId,
                                            @RequestHeader("X-User-Name") String username,
                                            @RequestHeader("X-User-Role") String role) {
            // VALIDACIÓN DE SEGURIDAD
            if (!"admin".equals(role) && !"empleado".equals(role)) {
                return ResponseEntity.status(403).body("Acceso denegado: Rol insuficiente");
            }
            
            try {
                LoanEntity loan = loanService.createLoan(clientId, toolId, username);
                return ResponseEntity.ok(loan);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
}