package com.example.UserService.config;

import com.example.UserService.filter.CustomUsernameAuthenticationFilter;
import com.example.UserService.filter.JwtValidationFilter;
import com.example.UserService.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SecurityConfig {
    @Autowired
    private WebClient webClient;
    private final UserDetailsService userDetailsService;
    private final Environment environment;
    private final UserService userService;

    public SecurityConfig(UserDetailsService userDetailsService, Environment environment, UserService userService) {
        this.userDetailsService = userDetailsService;
        this.environment = environment;
        this.userService = userService;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.GET,"/auth/getuserdetail/**").authenticated()
                        .requestMatchers("/auth/deleteuser/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                        .requestMatchers("/auth/register").permitAll()
                        .anyRequest().permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {

                            String session= "";
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                                for (Cookie cookie : cookies) {
                                    if ("session".equals(cookie.getName())) {
                                        session = cookie.getValue();
                                        cookie.setMaxAge(0);
                                        cookie.setPath("/");
                                        response.addCookie(cookie);
                                    }}}

                                webClient.delete()
                                        .uri("http://localhost:5555/delete/" + session)
                                        .retrieve()
                                        .bodyToMono(Void.class)
                                        .block();

                            response.setStatus(HttpServletResponse.SC_OK);
                        }));
        http.addFilterBefore(new CustomUsernameAuthenticationFilter("/auth/login", userService, authenticationManager, environment), UsernamePasswordAuthenticationFilter.class);
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
