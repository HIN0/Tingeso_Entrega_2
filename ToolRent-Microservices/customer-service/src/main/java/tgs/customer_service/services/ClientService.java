package tgs.customer_service.services;

import tgs.customer_service.entities.ClientEntity;
import tgs.customer_service.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public ClientEntity getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    public ClientEntity createClient(ClientEntity client) {
        if (clientRepository.findByRut(client.getRut()) != null) {
            throw new RuntimeException("El RUT ya existe");
        }
        if (client.getStatus() == null || client.getStatus().isEmpty()) {
            client.setStatus("ACTIVE");
        }
        return clientRepository.save(client);
    }
    
    public ClientEntity updateStatus(Long id, String newStatus) {
        ClientEntity client = getClientById(id);
        if (client != null) {
            client.setStatus(newStatus);
            return clientRepository.save(client);
        }
        return null;
    }

    public ClientEntity updateBalance(Long id, Double amount) {
        ClientEntity client = getClientById(id);
        if (client != null) {
            double current = client.getBalance() == null ? 0.0 : client.getBalance();
            client.setBalance(current + amount);
            
            // Regla de Negocio RF3.2: Si debe dinero (>0), pasa a RESTRINGIDO (RESTRICTED).
            // Si salda la deuda (<=0), vuelve a ACTIVE.
            if (client.getBalance() > 0) {
                client.setStatus("RESTRICTED");
            } else {
                client.setStatus("ACTIVE");
            }
            return clientRepository.save(client);
        }
        return null;
    }
}