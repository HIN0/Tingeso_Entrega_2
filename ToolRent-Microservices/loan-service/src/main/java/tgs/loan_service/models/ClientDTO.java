package tgs.loan_service.models;

import lombok.Data;

@Data
public class ClientDTO {
    private Long id;
    private String rut;
    private String name;
    private String status; // ACTIVO, RESTRINGIDO
}