package com.example.UserService.filter;

import com.example.UserService.constants.ApplicationConstants;
import com.example.UserService.entity.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CustomUsernameAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticationManager;
    private final Environment environment;

    public CustomUsernameAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, Environment environment) {
        super(defaultFilterProcessesUrl);
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
            generateAndSetJwt(authResult, response);
        }

        // Redirect to the user details page

        response.sendRedirect("http://localhost:8080/getuserdetail/rahul");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        System.out.println("Authentication failed for user: " + request.getParameter("username"));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }

    private void generateAndSetJwt(Authentication authentication, HttpServletResponse response) {
        if (authentication != null) {
            String username = authentication.getName(); // Get the username of the authenticated user

            // Retrieve the JWT secret key from the environment or properties
            String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

            // Generate the secret key using HMAC-SHA algorithm
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // Generate the JWT token
            String jwt = Jwts.builder()
                    .issuer("Eazy Bank")
                    .subject("JWT Token")
                    .claim("username", username)
                    .claim("authorities", "ROLE_USER,ROLE_ADMIN") // Or retrieve actual authorities dynamically
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 30000000)) // Set expiration time (adjust as needed)
                    .signWith(secretKey)
                    .compact();
            System.out.println("Creating JWT");

            // Create a cookie to store the JWT
            Cookie jwtCookie = new Cookie("JWT", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // Set to true in production
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24); // Set cookie expiration time

            // Add the cookie to the response
            response.addCookie(jwtCookie);

            System.out.println("JWT: " + jwt);
            //System.out.println("Response Headers: " + response.getHeaderNames());

            // Set the JWT token in the response header
            //response.setHeader(ApplicationConstants.JWT_HEADER, jwt);

            // Now set the authentication in the SecurityContext
            Authentication newAuth = new UsernamePasswordAuthenticationToken(username, null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_ADMIN"));
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        } else {
            System.out.println("User not authenticated");
        }
    }
}

