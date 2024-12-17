package com.example.Frontend.Controller;

import com.example.Frontend.entity.AuthRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
@Slf4j
public class UserController {

    private final WebClient webClient;


    public UserController( WebClient webClient) {
        this.webClient = webClient;
    }

    // Show Login page (GET request)
    @GetMapping("/login")
    public String loginPage() {
        return "login";  // Thymeleaf template for login form
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username,
                                   @RequestParam("password") String password,HttpServletResponse response) {

        WebClient webClient = WebClient.create("http://localhost:8080");
        AuthRequest authRequest = new AuthRequest(username, password);
        return webClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(authRequest))
                .exchangeToMono(clientResponse -> {
                    // Forward cookies from backend to the frontend
                    clientResponse.cookies().forEach((name, cookies) ->
                            cookies.forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString())));
                    return clientResponse.toBodilessEntity();
                })
                .block();
    }

    // Show Register page (GET request)
    @GetMapping("/register")
    public String registerPage() {
        return "register";  // Thymeleaf template for registration form
    }

    // Handle Register form submission (POST request)
    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password,
                               @RequestParam String email, @RequestParam String firstName,
                               @RequestParam String lastName, @RequestParam String phoneNumber,
                               @RequestParam String address, @RequestParam String city,
                               @RequestParam String country, @RequestParam String zipCode,
                               Model model) {
        //TODO
    return "";
    }
}


