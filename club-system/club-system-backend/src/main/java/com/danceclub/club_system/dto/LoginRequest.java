package com.danceclub.club_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 * 需求：1.2, 1.8
 */
public class LoginRequest {
    
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式無效")
    private String email;
    
    @NotBlank(message = "密碼不能為空")
    private String password;
    
    // Default constructor
    public LoginRequest() {}
    
    // Constructor with all fields
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}