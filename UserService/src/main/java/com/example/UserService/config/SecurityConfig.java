package com.example.UserService.config;

import com.example.UserService.filter.CustomUsernameAuthenticationFilter;
import com.example.UserService.filter.JwtValidationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final Environment environment;

    public SecurityConfig(UserDetailsService userDetailsService, Environment environment) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.GET,"/getuserdetail/**").authenticated()
                        .anyRequest().permitAll())
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    System.out.println("In successhandler");

                    // Fetch and print the JWT cookie
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            System.out.println(cookie.getName());
                            if ("JWT".equals(cookie.getName())) {
                                System.out.println("Found JWT cookie: " + cookie.getValue());
                            }
                        }
                    } else {
                        System.out.println("Cookies is null");
                    }

                    // Delete the JWT cookie
                    Cookie deleteCookie = new Cookie("JWT", null);
                    deleteCookie.setPath("/");
                    deleteCookie.setHttpOnly(true);
                    deleteCookie.setMaxAge(0); // Set the cookie to expire immediately
                    deleteCookie.setSecure(true);
                    response.addCookie(deleteCookie);

                    // Invalidate the security context
                    SecurityContextHolder.clearContext();

                    // Redirect to the login page
                    response.sendRedirect("http://localhost:8079/login");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JWT"));
        http.addFilterBefore(new CustomUsernameAuthenticationFilter("/login", authenticationManager,environment), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(new JwtValidationFilter(userDetailsService, environment), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
