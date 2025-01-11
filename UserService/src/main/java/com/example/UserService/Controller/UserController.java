package com.example.UserService.Controller;

import com.example.UserService.dto.UserDTO;
import com.example.UserService.entity.AuthRequest;
import com.example.UserService.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public String logins(@RequestBody AuthRequest authRequest) {
        System.out.println("In 8080");
        System.out.println(authRequest.getPassword());
        System.out.println(authRequest.getUsername());
        return userService.authenticateUser(authRequest.getUsername(), authRequest.getPassword());
    }


    // Handle Register form submission (POST request)
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        System.out.println("Coming here");
        String response = userService.registerUser(userDTO); // Call service for registration
        if (response.equals("success")) {
            return ResponseEntity.ok("Registration successful");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed");
        }
    }

    @ResponseBody
    @GetMapping("/getuserdetail/{username}")
    public UserDTO userdetail(@PathVariable String username, Model model) {
        UserDTO userdetail = userService.getUserDetails(username);
        return userdetail;
    }

    @GetMapping("/deleteuser/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        try {
            String response = userService.deleteUser(username);
            return ResponseEntity.ok("User " + username + " deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User " + username + " not found.");
        }
    }




}
