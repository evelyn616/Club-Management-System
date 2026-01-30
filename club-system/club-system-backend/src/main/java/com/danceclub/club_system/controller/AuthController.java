package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.JwtResponse;
import com.danceclub.club_system.dto.LoginRequest;
import com.danceclub.club_system.dto.RegisterRequest;
import com.danceclub.club_system.dto.UserResponse;
import com.danceclub.club_system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations
 * 需求：1.1, 1.2, 1.8, 1.9
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * User registration endpoint
     * POST /api/auth/register
     * 需求：1.2, 1.3
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            JwtResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("REGISTRATION_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "註冊過程中發生錯誤"));
        }
    }
    
    /**
     * Member login endpoint (for member login interface)
     * POST /api/auth/login
     * 需求：1.8
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("LOGIN_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "登入過程中發生錯誤"));
        }
    }
    
    /**
     * Admin login endpoint (for admin backend interface)
     * POST /api/auth/admin/login
     * 需求：1.9
     */
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.adminLogin(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ADMIN_LOGIN_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "管理員登入過程中發生錯誤"));
        }
    }
    
    /**
     * Get current user information
     * GET /api/auth/me
     * 需求：1.8
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("UNAUTHORIZED", "用戶未登入"));
            }
            
            String email = authentication.getName();
            UserResponse userResponse = authService.getCurrentUser(email);
            
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("USER_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "獲取用戶信息時發生錯誤"));
        }
    }
    
    /**
     * Error response DTO for consistent error handling
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        // Getters
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        // Setters
        public void setCode(String code) {
            this.code = code;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}