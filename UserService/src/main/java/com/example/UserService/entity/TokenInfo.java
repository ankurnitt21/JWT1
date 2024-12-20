package com.example.UserService.entity;

public class TokenInfo {
    private String username;
    private String sessionid;
    private String jwt;


    public TokenInfo(String username, String sessionid, String jwt) {
        this.username = username;
        this.sessionid = sessionid;
        this.jwt = jwt;
    }

    public TokenInfo() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}