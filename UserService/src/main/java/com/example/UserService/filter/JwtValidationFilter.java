package com.example.UserService.filter;

import javax.crypto.SecretKey;

import com.example.UserService.entity.TokenInfo;
import jakarta.servlet.http.Cookie;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.example.UserService.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

public class JwtValidationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public JwtValidationFilter(UserDetailsService userDetailsService, Environment environment) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    private String getJwtFromCookies(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        String session= "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session".equals(cookie.getName())) {
                     session = cookie.getValue();}}}
       // String username = (String) request.getSession().getAttribute("username");

        WebClient webClient = WebClient.create("http://localhost:5555");
        String finalsession = session;
        ResponseEntity<TokenInfo> responseEntity = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/get/" + finalsession).build())
                .retrieve()
                .toEntity(TokenInfo.class)
                .block();
        if (responseEntity != null && responseEntity.getBody() != null) {
            return responseEntity.getBody().getJwt();
        } else {
            return null;
        }
    }

    private void validateJwt(String jwt, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        try {
            System.out.println("ss");
            System.out.println(jwt);
            // Retrieve the secret key from the environment
            String secret = environment.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // Parse the JWT and extract the claims
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            // Extract username and authorities from claims
            String username = claims.get("username", String.class);
            String authorities = claims.get("authorities", String.class);

            // Set the authentication in the SecurityContext
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // If token parsing/validation fails, log and send error
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
        }
    }

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String jwt = getJwtFromCookies(request,response);
            if (jwt != null) {
                System.out.println("Found JWT Validating");
                validateJwt(jwt, response);
            } else {
                System.out.println("Didn't found JWT");
                response.sendRedirect("http://localhost:8079/login");
            }
        }
        chain.doFilter(request, response);
    }
}