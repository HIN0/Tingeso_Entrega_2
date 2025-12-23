package tgs.auth_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgs.auth_service.dtos.AuthUserDto;
import tgs.auth_service.dtos.NewUserDto;
import tgs.auth_service.dtos.TokenDto;
import tgs.auth_service.entities.UserEntity;
import tgs.auth_service.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody AuthUserDto dto){
        TokenDto tokenDto = authService.login(dto);
        if(tokenDto == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> create(@RequestBody NewUserDto dto){
        UserEntity user = authService.save(dto);
        if(user == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(user);
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenDto> validate(@RequestParam String token){
        TokenDto tokenDto = authService.validate(token);
        if(tokenDto == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(tokenDto);
    }
}