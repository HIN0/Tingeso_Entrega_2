package tgs.customer_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;

    private String name;
    private String email;
    private String phone;

    // Estado: "ACTIVO" o "RESTRINGIDO"
    // Regla de negocio: Si está restringido, no puede pedir préstamos.
    private String status; 
}