package com.example.UserService.filter;

import com.example.UserService.constants.ApplicationConstants;
import com.example.UserService.dto.UserDTO;
import com.example.UserService.entity.AuthRequest;
import com.example.UserService.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

public class CustomUsernameAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final Environment environment;

    public CustomUsernameAuthenticationFilter(String defaultFilterProcessesUrl, UserService userService, AuthenticationManager authenticationManager, Environment environment) {
        super(defaultFilterProcessesUrl);
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.environment = environment;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        AuthRequest authRequest = new ObjectMapper().readValue(request.getInputStream(), AuthRequest.class);
        System.out.println("Attempting authentication for user: " + authRequest.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String existingJwt = request.getHeader(ApplicationConstants.JWT_HEADER);
        if (existingJwt == null || existingJwt.isEmpty()) {
            generateAndSetJwt(authResult, response,request);
        }


        response.sendRedirect("http://localhost:8080/getuserdetail/ankur");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        System.out.println("Authentication failed for user: " + request.getParameter("username"));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }

    private void generateAndSetJwt(Authentication authentication, HttpServletResponse response, HttpServletRequest request) {
        if (authentication != null) {
            String username = authentication.getName(); // Get the username of the authenticated user
           // request.getSession().setAttribute("username", username);

            // Retrieve the JWT secret key from the environment or properties
            String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

            // Generate the secret key using HMAC-SHA algorithm
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            String role = userService.getRole(username);

            // Generate the JWT token
            String jwt = Jwts.builder()
                    .issuer("Testing")
                    .subject("JWT Token")
                    .claim("username", username)
                    .claim("authorities", "ROLE_"+role) // Or retrieve actual authorities dynamically
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3000000)) // Set expiration time (adjust as needed)
                    .signWith(secretKey)
                    .compact();
            System.out.println("Creating JWT");

            System.out.println("JWT: " + jwt);

            WebClient webClient = WebClient.create("http://localhost:5555");
            ResponseEntity<String> responseEntity =webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/store")
                            .queryParam("token", jwt)
                            .queryParam("username", username)
                            .build())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            System.out.println("Returned from redis");
            System.out.println(responseEntity);
            System.out.println(responseEntity.getBody());

            Cookie sessioncookie = new Cookie("session", responseEntity.getBody());
            sessioncookie.setPath("/");
            //usernameCookie.setHttpOnly(true); // Optional: Makes the cookie inaccessible to JavaScript
            //usernameCookie.setMaxAge(60 * 60); // Optional: Sets the cookie to expire in 1 hour
            response.addCookie(sessioncookie);

            // Now set the authentication in the SecurityContext
            Authentication newAuth = new UsernamePasswordAuthenticationToken(username, null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_"+role));
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        } else {
            System.out.println("User not authenticated");
        }
    }
}