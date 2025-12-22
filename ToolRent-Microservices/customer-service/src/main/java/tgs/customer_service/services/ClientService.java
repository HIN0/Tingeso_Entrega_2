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
        // Regla de negocio: Cliente nuevo nace ACTIVO
        if (client.getStatus() == null) {
            client.setStatus("ACTIVO");
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
}