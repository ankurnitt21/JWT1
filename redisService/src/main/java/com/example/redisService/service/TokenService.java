package com.example.redisService.service;

import com.example.redisService.entity.TokenInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String storeToken(String token, String username) {
        System.out.println(token+username);
        System.out.println("In redis service1");
        String pattern = "user:" + username + ":session:*";
        System.out.println(pattern);
        String correct_pattern = redisTemplate.keys(pattern).toString();
        System.out.println(correct_pattern);
        String cleanedKey = correct_pattern.replace("[", "").replace("]", "");
        System.out.println(cleanedKey);
        System.out.println("In redis service4");
        String all[] = cleanedKey.split(":");
        if(all.length==4){
            System.out.println(cleanedKey);
            System.out.println("keys already present " + cleanedKey);
            return all[3];
        }
        String sessionId = UUID.randomUUID().toString();
        String key = "user:" + username + ":session:" + sessionId;
        redisTemplate.opsForValue().set(key, token, 1, TimeUnit.HOURS);
        return sessionId;
    }

    public TokenInfo getToken(String session) {
        String pattern = "user:*" + ":session:" + session;
        String correct_pattern = redisTemplate.keys(pattern).toString();
        String cleanedKey = correct_pattern.replace("[", "").replace("]", "");
        System.out.println(cleanedKey);
        String all[] = cleanedKey.split(":");
        String user = all[1];
        String sessionid = all[3];
        String value = (String) redisTemplate.opsForValue().get(cleanedKey);
        System.out.println(user);
        System.out.println(sessionid);
        System.out.println(value);
        return new TokenInfo(user,sessionid,value);
    }

    public String deleteToken(String session) {
        System.out.println("Deleting jwt from redis");
        System.out.println(session);
        // Logic to delete the token from Redis
        String pattern = "user:*" + ":session:"+session;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(pattern);
        return "Token deleted successfully";
    }
}