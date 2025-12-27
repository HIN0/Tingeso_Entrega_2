package tgs.report_service.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

    private Long id;
    private String rut;
    private String name;
    private String email;
    private String phone;
    private String status;
    private Double balance;
}