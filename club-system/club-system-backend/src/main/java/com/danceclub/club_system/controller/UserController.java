package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.ChangePasswordRequest;
import com.danceclub.club_system.dto.UpdateUserRequest;
import com.danceclub.club_system.dto.UpdateUserRoleRequest;
import com.danceclub.club_system.dto.UserResponse;
import com.danceclub.club_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for user management operations
 * 需求：1.11, 2.2
 * 
 * Note: Security annotations are temporarily disabled until Spring Security is added to the project
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Get user data by ID
     * GET /api/users/:id
     * 需求：1.11
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        try {
            UserResponse userResponse = userService.getUserById(id);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update user profile information
     * PUT /api/users/:id
     * 需求：1.11
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request) {
        try {
            UserResponse userResponse = userService.updateUser(id, request);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update user role (admin only)
     * PUT /api/admin/users/:id/role
     * 需求：2.2
     * 
     * TODO: Add @PreAuthorize("hasRole('ADMIN')") when Spring Security is configured
     */
    @PutMapping("/admin/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String id,
            @RequestBody UpdateUserRoleRequest request) {
        try {
            UserResponse userResponse = userService.updateUserRole(id, request);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Change user password
     * PUT /api/users/:id/password
     * 需求：1.11
     */
    @PutMapping("/users/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable String id,
            @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "密碼修改成功");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "密碼修改失敗");
            return ResponseEntity.badRequest().body(error);
        }
    }
}