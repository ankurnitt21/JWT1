package com.example.Frontend.Controller;

import com.example.Frontend.dto.UserDTO;
import com.example.Frontend.entity.AuthRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.UnsupportedEncodingException;

@Controller
@RequestMapping("/users")
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
        System.out.println("Here 41");
        WebClient webClient = WebClient.create("http://localhost:8078");
        AuthRequest authRequest = new AuthRequest(username, password);
        return webClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(authRequest))
                .exchangeToMono(clientResponse -> clientResponse.toBodilessEntity())
                .block();
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        WebClient webClient = WebClient.create("http://localhost:8078");
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/auth/logout")
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
        System.out.println("Coming here");
        WebClient webClient = WebClient.create("http://localhost:8078");
        UserDTO userDTO = new UserDTO(username, password,email,firstName,lastName,phoneNumber,address,city,country,zipCode);
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/auth/register")
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
                    response.sendRedirect("http://localhost:8078/users/login"); // Redirect to the desired page
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("in catch");
                    response.sendRedirect("http://localhost:8078/users/register");
                }
            }
        } else {
            System.out.println("No response received from the backend service.");
        }

        return responseEntity;

    }
}


