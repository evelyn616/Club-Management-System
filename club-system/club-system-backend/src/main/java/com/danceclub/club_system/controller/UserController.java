package com.club.management.controller;

import com.club.management.dto.UpdateUserRequest;
import com.club.management.dto.UpdateUserRoleRequest;
import com.club.management.dto.UserResponse;
import com.club.management.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management operations
 * 需求：1.11, 2.2
 */
@RestController
@RequestMapping("/api")
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
     */
    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
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
}