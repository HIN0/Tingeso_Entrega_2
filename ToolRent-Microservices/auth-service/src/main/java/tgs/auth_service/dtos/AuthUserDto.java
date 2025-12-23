package tgs.auth_service.dtos;

import lombok.Data;

@Data
public class AuthUserDto {
    private String username;
    private String password;
}