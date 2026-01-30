package com.danceclub.club_system.dto;

/**
 * DTO for JWT authentication response
 * 需求：1.8, 1.9
 */
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private UserResponse user;
    
    // Default constructor
    public JwtResponse() {}
    
    // Constructor
    public JwtResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
}