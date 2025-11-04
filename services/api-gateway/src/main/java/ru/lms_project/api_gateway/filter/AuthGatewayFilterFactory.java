package ru.lms_project.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ru.lms_project.common.security.JwtTokenProvider;
import ru.lms_project.common.security.ParsedToken;

@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    private final JwtTokenProvider jwtTokenProvider;

    public AuthGatewayFilterFactory(JwtTokenProvider jwtTokenProvider) {
        super(Object.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            try {
                ParsedToken parsedToken = jwtTokenProvider.parseAccessToken(authHeader);

                ServerHttpRequest newRequest = request.mutate()
                        .header("X-User-Id", parsedToken.getUserId().toString())
                        .header("X-User-Roles", String.join(",", parsedToken.getRoles()))
                        .build();

                ServerWebExchange newExchange = exchange.mutate()
                        .request(newRequest)
                        .build();

                return chain.filter(newExchange);

            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        };
    }
}