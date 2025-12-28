package com.club.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating user profile information
 * 需求：1.11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    /**
     * 用戶姓名
     */
    private String name;
    
    /**
     * 電子郵件
     */
    private String email;
    
    /**
     * 電話號碼
     */
    private String phone;
    
    /**
     * 生日
     */
    private LocalDate birthday;
}