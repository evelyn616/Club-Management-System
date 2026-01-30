package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.UpdateUserRequest;
import com.danceclub.club_system.dto.UpdateUserRoleRequest;
import com.danceclub.club_system.dto.UserResponse;
import com.danceclub.club_system.model.User;
import com.danceclub.club_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Service for user management operations
 * 需求：1.11, 2.2, 14.2, 14.3
 * 
 * Note: Security features are temporarily disabled until Spring Security is added to the project
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
     * 
     * TODO: Add authentication check when Spring Security is configured
     */
    public UserResponse getUserById(String userId) {
        // Find target user
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // TODO: Check permissions - users can only access their own data unless they are admin
        
        return convertToUserResponse(targetUser);
    }
    
    /**
     * Update user profile information
     * 需求：1.11, 14.2, 14.3
     * 
     * TODO: Add authentication check when Spring Security is configured
     */
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        // Find target user
        User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // TODO: Check permissions - users can only update their own data unless they are admin
        
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
        
        if (request.getSchool() != null) {
            targetUser.setSchool(request.getSchool().trim().isEmpty() ? null : request.getSchool().trim());
        }
        
        // Save updated user (updatedAt will be automatically set by @PreUpdate)
        User updatedUser = userRepository.save(targetUser);
        
        return convertToUserResponse(updatedUser);
    }
    
    /**
     * Update user role (admin only)
     * 需求：2.2
     * 
     * TODO: Add admin authentication check when Spring Security is configured
     */
    @Transactional
    public UserResponse updateUserRole(String userId, UpdateUserRoleRequest request) {
        // TODO: Check if current user is admin
        
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
            user.getSchool(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}