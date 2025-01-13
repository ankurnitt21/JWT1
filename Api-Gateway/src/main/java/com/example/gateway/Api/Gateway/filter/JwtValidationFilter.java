package com.example.gateway.Api.Gateway.filter;

import com.example.gateway.Api.Gateway.constants.ApplicationConstants;
import com.example.gateway.Api.Gateway.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtValidationFilter implements GlobalFilter {

    private final Environment environment;

    public JwtValidationFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Hitting filter");

        String path = exchange.getRequest().getURI().getPath();
        System.out.println("Request URI: " + path);

        // Skip JWT validation for certain paths (e.g., login, register)
        if ("/users/register".equals(path)) {
            return chain.filter(exchange);
        }
        if ("/users/login".equals(path)) {
            return chain.filter(exchange);
        }
        if ("/auth/login".equals(path)) {
            return chain.filter(exchange);
        }
        if ("/auth/register".equals(path)) {
            return chain.filter(exchange);
        }

        // Retrieve and validate JWT token
        String jwt = getJwtFromCookies(exchange);
        if (jwt != null) {
            try {
                validateJwt(jwt);
                return chain.filter(exchange);  // Continue the chain
            } catch (Exception e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        } else {
            // If JWT is not found or invalid
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    private String getJwtFromCookies(ServerWebExchange exchange) {
        System.out.println("Hitting filter2");
        String session = "";
        List<String> cookies = exchange.getRequest().getHeaders().get("Cookie");

        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("session")) {
                    session = cookie.split("=")[1];  // Assuming cookie is like "session=<value>"
                    break;
                }
            }
        }

        if (session.isEmpty()) return null;

        WebClient webClient = WebClient.create("http://localhost:5555");
        String finalSession = session;

        // Use WebClient to fetch JWT token based on session
        TokenInfo tokenInfo = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/get/" + finalSession).build())
                .retrieve()
                .bodyToMono(TokenInfo.class)
                .block();

        if (tokenInfo != null) {
            return tokenInfo.getJwt();
        } else {
            return null;
        }
    }

    private void validateJwt(String jwt) throws Exception {
        System.out.println("Hitting filter3");
        String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // Parse the JWT and extract the claims
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        // Extract username and authorities from claims (optional)
        String username = claims.get("username", String.class);
        String authorities = claims.get("authorities", String.class);
    }
}