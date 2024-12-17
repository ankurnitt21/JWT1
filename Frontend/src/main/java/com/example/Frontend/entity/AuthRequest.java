package com.example.Frontend.entity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthRequest {
    private String username;
    private String password;

    // Constructors, getters, and setters
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
