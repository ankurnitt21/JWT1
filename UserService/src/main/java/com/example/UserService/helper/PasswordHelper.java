package com.example.UserService.helper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHelper {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodePassword){
        return passwordEncoder.matches(rawPassword,encodePassword);
    }
}
