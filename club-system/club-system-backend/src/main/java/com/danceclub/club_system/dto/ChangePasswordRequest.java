package com.danceclub.club_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for password change request
 */
public class ChangePasswordRequest {
    
    @NotBlank(message = "舊密碼不能為空")
    private String oldPassword;
    
    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 100, message = "新密碼長度必須在6-100個字符之間")
    private String newPassword;
    
    // Default constructor
    public ChangePasswordRequest() {}
    
    // Constructor
    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
    
    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
