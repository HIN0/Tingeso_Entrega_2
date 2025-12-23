package tgs.auth_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tgs.auth_service.dtos.AuthUserDto;
import tgs.auth_service.dtos.NewUserDto;
import tgs.auth_service.dtos.TokenDto;
import tgs.auth_service.entities.UserEntity;
import tgs.auth_service.repositories.UserRepository;
import tgs.auth_service.security.JwtProvider;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    public UserEntity save(NewUserDto dto) {
        Optional<UserEntity> user = userRepository.findByUsername(dto.getUsername());
        if(user.isPresent())
            return null;
        
        String password = passwordEncoder.encode(dto.getPassword());
        UserEntity userEntity = UserEntity.builder()
                .username(dto.getUsername())
                .password(password)
                .role(dto.getRole())
                .build();
        return userRepository.save(userEntity);
    }

    public TokenDto login(AuthUserDto dto) {
        Optional<UserEntity> user = userRepository.findByUsername(dto.getUsername());
        if(!user.isPresent())
            return null;
        
        if(passwordEncoder.matches(dto.getPassword(), user.get().getPassword())) {
            return new TokenDto(jwtProvider.createToken(user.get()));
        }
        return null;
    }

    public TokenDto validate(String token) {
        if(!jwtProvider.validate(token))
            return null;
        
        String username = jwtProvider.getUserNameFromToken(token);
        if(!userRepository.findByUsername(username).isPresent())
            return null;
        
        return new TokenDto(token);
    }
}