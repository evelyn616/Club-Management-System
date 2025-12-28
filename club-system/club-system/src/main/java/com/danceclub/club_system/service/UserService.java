package com.club.management.service;

import com.club.management.dto.UpdateUserRequest;
import com.club.management.dto.UpdateUserRoleRequest;
import com.club.management.dto.UserResponse;
import com.club.management.entity.User;
import com.club.management.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Service for user management operations
 * 需求：1.11, 2.2, 14.2, 14.3
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get user data by ID
     * 需求：1.11, 14.2, 14.3
     */
    public UserResponse getUserById(String userId) {
        // Get current authenticated user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new IllegalArgumentException("當前用戶不存在"));
        
        // Find target user
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Check permissions - users can only access their own data unless they are admin
        if (!isCurrentUserAdmin() && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("權限不足，只能查看自己的個人資料");
        }
        
        return convertToUserResponse(targetUser);
    }
    
    /**
     * Update user profile information
     * 需求：1.11, 14.2, 14.3
     */
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        // Get current authenticated user
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new IllegalArgumentException("當前用戶不存在"));
        
        // Find target user
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Check permissions - users can only update their own data unless they are admin
        if (!isCurrentUserAdmin() && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("權限不足，只能修改自己的個人資料");
        }
        
        // Validate email format if provided
        if (request.getEmail() != null && !isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("電子郵件格式無效");
        }
        
        // Check email uniqueness if email is being changed
        if (request.getEmail() != null && !request.getEmail().equals(targetUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("此電子郵件已被使用");
            }
        }
        
        // Update user fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            targetUser.setName(request.getName().trim());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            targetUser.setEmail(request.getEmail().trim());
        }
        
        if (request.getPhone() != null) {
            targetUser.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }
        
        if (request.getBirthday() != null) {
            targetUser.setBirthday(request.getBirthday());
        }
        
        // Save updated user (updatedAt will be automatically set by @PreUpdate)
        User updatedUser = userRepository.save(targetUser);
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Update user role (admin only)
     * 需求：2.2
     */
    @Transactional
    public UserResponse updateUserRole(String userId, UpdateUserRoleRequest request) {
        // Check if current user is admin
        if (!isCurrentUserAdmin()) {
            throw new AccessDeniedException("權限不足，僅限管理員執行此操作");
        }
        
        // Find target user
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Validate role
        if (request.getRole() == null || 
            (!request.getRole().equals("member") && !request.getRole().equals("admin"))) {
            throw new IllegalArgumentException("角色必須為 'member' 或 'admin'");
        }
        
        // Update role
        targetUser.setRole(request.getRole());
        
        // Save updated user (updatedAt will be automatically set by @PreUpdate)
        User updatedUser = userRepository.save(targetUser);
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Check if current user is admin
     */
    private boolean isCurrentUserAdmin() {
        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElse(null);
        
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
    
    /**
     * Get current authenticated user's email
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用戶未登入");
        }
        return authentication.getName();
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getBirthday(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}