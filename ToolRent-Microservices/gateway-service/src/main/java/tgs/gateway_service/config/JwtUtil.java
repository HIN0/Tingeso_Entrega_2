package tgs.gateway_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    private Key secret;

    @PostConstruct
    public void init() {
        String secretStr = "UnaClaveMuySecretaYLoSuficientementeLargaParaToolRent2025";
        secret = Keys.hmacShaKeyFor(secretStr.getBytes(StandardCharsets.UTF_8));
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    public void validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
    }
    
    public String getRole(String token) {
        return getAllClaimsFromToken(token).get("role", String.class);
    }
}