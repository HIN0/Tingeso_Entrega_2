package tgs.gateway_service.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import tgs.gateway_service.config.JwtUtil;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @SuppressWarnings("null")
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }
            // 1. Verificar si la ruta requiere token (Login/Registro son públicos)
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                // 2. Validar Token
                jwtUtil.validateToken(authHeader);

                // 3. Extraer Rol
                String role = jwtUtil.getRole(authHeader); // "admin" o "employee"
                
                // 4. Reglas de Negocio (Protección de Rutas)
                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethod().name();

                // REGLA: Solo ADMIN puede modificar Inventario (POST, PUT, DELETE)
                if (path.contains("/api/tools") && !method.equals("GET")) {
                    if (!"admin".equalsIgnoreCase(role)) return onError(exchange, HttpStatus.FORBIDDEN);
                }
                
                // REGLA: Solo ADMIN puede modificar Tarifas
                if (path.contains("/api/tariffs") && !method.equals("GET")) {
                    if (!"admin".equalsIgnoreCase(role)) return onError(exchange, HttpStatus.FORBIDDEN);
                }
                
                // REGLA: Solo ADMIN puede dar de baja (Inventory)
                // (Ya cubierto arriba por el filtro general de tools)

            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private reactor.core.publisher.Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuración vacía
    }
}