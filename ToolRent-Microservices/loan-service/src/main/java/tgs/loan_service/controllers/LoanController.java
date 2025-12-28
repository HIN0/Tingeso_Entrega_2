package tgs.loan_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgs.loan_service.entitites.LoanEntity;
import tgs.loan_service.models.LoanDetailDTO;
import tgs.loan_service.models.ReturnLoanDTO;
import tgs.loan_service.services.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;


    //@GetMapping
    //public ResponseEntity<List<LoanEntity>> getAll() {
        //return ResponseEntity.ok(loanService.getAllLoans());
    //}


    @GetMapping
    public ResponseEntity<List<LoanDetailDTO>> getAllLoans() {
        List<LoanDetailDTO> loans = loanService.findAllWithDetails();
        if (loans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loans);
    }

    // Endpoint para CREAR préstamo
    @PostMapping
    public ResponseEntity<LoanEntity> createLoan(
            @RequestParam Long clientId, 
            @RequestParam Long toolId, 
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        // Nota: Asumimos que el Gateway o un Filtro pasa el usuario en el header. 
        // Si no tienes seguridad implementada aún, usa un valor por defecto.
        return ResponseEntity.ok(loanService.createLoan(clientId, toolId, username));
    }

    // Endpoint para DEVOLVER préstamo (Aquí está la magia de la integración)
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanEntity> returnLoan(
            @PathVariable Long id,
            @RequestBody ReturnLoanDTO dto,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String username) {
        
        // 1. Traducir los booleanos del Frontend a la Condición del Backend
        String condition = "GOOD";
        if (Boolean.TRUE.equals(dto.getDamaged())) {
            condition = "DAMAGED";
            if (Boolean.TRUE.equals(dto.getIrreparable())) {
                condition = "DESTROYED";
            }
        }

        // 2. Llamar al servicio corregido
        // Nota: Ignoramos dto.getReturnDate() por seguridad (usamos LocalDate.now() en el servicio),
        // o puedes modificar el servicio para aceptarla si es un requerimiento permitir fechas pasadas.
        LoanEntity loan = loanService.returnLoan(id, username, condition);
        
        return ResponseEntity.ok(loan);
    }
}