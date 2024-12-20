package com.example.redisService.service;

import com.example.redisService.entity.TokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String storeToken(String token, String username) {
        String pattern = "user:" + username + ":session:*";
        String correct_pattern = redisTemplate.keys(pattern).toString();
        String cleanedKey = correct_pattern.replace("[", "").replace("]", "");
        System.out.println(cleanedKey);
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

    public TokenInfo getToken(String username) {
        String pattern = "user:" + username + ":session:*";
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
}