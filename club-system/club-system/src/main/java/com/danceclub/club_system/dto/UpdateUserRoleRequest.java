package com.club.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user role (admin only)
 * 需求：2.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    
    /**
     * 用戶角色（'member' 或 'admin'）
     */
    private String role;
}