package tgs.customer_service.controllers;

import tgs.customer_service.entities.ClientEntity;
import tgs.customer_service.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientEntity>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientEntity> getClientById(@PathVariable Long id) {
        ClientEntity client = clientService.getClientById(id);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody ClientEntity client) {
        try {
            return ResponseEntity.ok(clientService.createClient(client));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<ClientEntity> updateBalance(@PathVariable Long id, @RequestParam Double amount) {
        try {
            ClientEntity updatedClient = clientService.updateBalance(id, amount);
            if (updatedClient == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ClientEntity> payDebt(@PathVariable Long id, @RequestParam Double amount) {
        ClientEntity updated = clientService.payDebt(id, amount);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Endpoint para Restringir/Activar manualmente
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam String newStatus) {
        try {
            ClientEntity updated = clientService.changeStatus(id, newStatus);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}