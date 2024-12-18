package com.example.UserService.filter;

import javax.crypto.SecretKey;

import jakarta.servlet.http.Cookie;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.example.UserService.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtValidationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public JwtValidationFilter(UserDetailsService userDetailsService, Environment environment) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    private String getJwtFromCookies(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ApplicationConstants.JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        //return response.getHeader("Authorization");
        return null;
    }

    private void validateJwt(String jwt, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        try {
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
            }
        }
        chain.doFilter(request, response);
    }
}