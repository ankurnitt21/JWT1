package com.example.Frontend.Controller;

import com.example.Frontend.dto.UserDTO;
import com.example.Frontend.entity.AuthRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Controller
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
                    return clientResponse.toBodilessEntity();
                })
                .block();
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        WebClient webClient = WebClient.create("http://localhost:8080");
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .block();

        // Log or process the response
        if (responseEntity != null) {
            System.out.println("Response Status Code: " + responseEntity.getStatusCode());
            System.out.println("Response Body: " + responseEntity.getBody());

            if (responseEntity.getStatusCode() == HttpStatus.FOUND) {
                String location = responseEntity.getHeaders().getLocation().toString();
                System.out.println("Redirecting to: " + location);
                try {
                    response.sendRedirect(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No response received from the backend service.");
        }
    }

    // Show Register page (GET request)
    @GetMapping("/register")
    public String registerPage() {
        return "register";  // Thymeleaf template for registration form
    }

    // Handle Register form submission (POST request)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password,
                               @RequestParam String email, @RequestParam String firstName,
                               @RequestParam String lastName, @RequestParam String phoneNumber,
                               @RequestParam String address, @RequestParam String city,
                               @RequestParam String country, @RequestParam String zipCode,
                               HttpServletResponse response) throws IOException {

        WebClient webClient = WebClient.create("http://localhost:8080");
        UserDTO userDTO = new UserDTO(username, password,email,firstName,lastName,phoneNumber,address,city,country,zipCode);
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(userDTO))
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .block();

        // Log or process the response
        if (responseEntity != null) {
            System.out.println("Response Status Code: " + responseEntity.getStatusCode());
            System.out.println("Response Body: " + responseEntity.getBody());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    response.sendRedirect("/login"); // Redirect to the desired page
                } catch (IOException e) {
                    e.printStackTrace();
                    response.sendRedirect("/register");
                }
            }
        } else {
            System.out.println("No response received from the backend service.");
        }

        return responseEntity;

    }
}


