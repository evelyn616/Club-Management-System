package com.club.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for user registration request
 * 需求：1.2, 1.3, 1.8
 */
public class RegisterRequest {
    
    @NotBlank(message = "姓名不能為空")
    @Size(max = 100, message = "姓名長度不能超過100個字符")
    private String name;
    
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式無效")
    @Size(max = 255, message = "電子郵件長度不能超過255個字符")
    private String email;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在6-100個字符之間")
    private String password;
    
    @NotBlank(message = "電話號碼不能為空")
    @Size(max = 20, message = "電話號碼長度不能超過20個字符")
    private String phone;
    
    @NotNull(message = "生日不能為空")
    private LocalDate birthday;
    
    // Default constructor
    public RegisterRequest() {}
    
    // Constructor with all fields
    public RegisterRequest(String name, String email, String password, String phone, LocalDate birthday) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.birthday = birthday;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}