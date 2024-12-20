package com.example.redisService.controller;

import com.example.redisService.entity.TokenInfo;
import com.example.redisService.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RedisController {
    @Autowired
    private TokenService tokenService;

    @PostMapping("/store")
    public String storeit(@RequestParam String token, @RequestParam String username) {
        System.out.println(token);
        System.out.println(username);
        return tokenService.storeToken(token,username);
    }

    @GetMapping("/get/{username}")
    public TokenInfo getToken(@PathVariable String username) {
        return tokenService.getToken(username);
    }
}
