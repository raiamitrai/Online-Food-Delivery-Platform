package com.quickbite.gateway.filter;

import com.quickbite.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    // Public endpoints that bypass authentication
    private final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/api/notifications",
            "/api/reviews"
    );

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            
            // Check if route is public
            boolean isSecuredPath = isSecured(exchange);
            System.out.println(">>> GATEWAY REQUEST: " + exchange.getRequest().getURI().getPath() + " | IS_SECURED: " + isSecuredPath);
            
            if (isSecuredPath) {
                // Check if Authorization header exists
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // Validate token
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    System.out.println("Invalid access...!");
                    return onError(exchange, "Unauthorized access", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private boolean isSecured(ServerWebExchange exchange) {
        final String path = exchange.getRequest().getURI().getPath();
        final String method = exchange.getRequest().getMethod().name();

        // Allow everyone to see the restaurant list (GET /api/restaurants)
        if (path.equals("/api/restaurants") && "GET".equalsIgnoreCase(method)) {
            return false;
        }

        return openApiEndpoints.stream().noneMatch(uri -> path.contains(uri));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
