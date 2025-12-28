package tgs.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import tgs.auth_service.entities.UserEntity;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    private Key secret;

    @PostConstruct
    protected void init() {
        String secretStr = "UnaClaveMuySecretaYLoSuficientementeLargaParaToolRent2025";
        secret = Keys.hmacShaKeyFor(secretStr.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(UserEntity authUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authUser.getRole());
        claims.put("id", authUser.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(authUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
                .signWith(secret)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserNameFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secret).build()
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return "bad token";
        }
    }
}