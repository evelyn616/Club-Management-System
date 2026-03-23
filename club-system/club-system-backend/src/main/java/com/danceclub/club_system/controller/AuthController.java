package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.JwtResponse;
import com.danceclub.club_system.dto.LoginRequest;
import com.danceclub.club_system.dto.RegisterRequest;
import com.danceclub.club_system.dto.UserResponse;
import com.danceclub.club_system.service.AuthService;
import com.danceclub.club_system.service.MfaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication operations
 * 需求：1.1, 1.2, 1.8, 1.9
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    private final MfaService mfaService;

    public AuthController(AuthService authService, MfaService mfaService) {
        this.authService = authService;
        this.mfaService = mfaService;
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
     * 發送 MFA 驗證碼（需已登入）
     * POST /api/auth/admin/mfa/send
     * Body: { "type": "EMAIL" | "PHONE" }
     */
    @PostMapping("/admin/mfa/send")
    public ResponseEntity<?> sendMfaCode(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName(); // JWT subject = userId
            String type = body.getOrDefault("type", "EMAIL");
            mfaService.sendCode(userId, type);

            String masked = "EMAIL".equalsIgnoreCase(type)
                    ? mfaService.getMaskedEmail(userId)
                    : mfaService.getMaskedPhone(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "驗證碼已發送",
                    "maskedTarget", masked
            ));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("SMS_NOT_CONFIGURED", "SMS 服務尚未設定，請選擇信箱驗證"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("MFA_SEND_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "發送驗證碼失敗：" + e.getMessage()));
        }
    }

    /**
     * 驗證 MFA 驗證碼（需已登入）
     * POST /api/auth/admin/mfa/verify
     * Body: { "code": "123456" }
     */
    @PostMapping("/admin/mfa/verify")
    public ResponseEntity<?> verifyMfaCode(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            String code = body.get("code");
            if (code == null || code.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("INVALID_CODE", "請輸入驗證碼"));
            }
            boolean valid = mfaService.verifyCode(userId, code);
            if (valid) {
                return ResponseEntity.ok(Map.of("success", true, "message", "驗證成功"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("WRONG_CODE", "驗證碼錯誤或已過期"));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("MFA_BLOCKED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "驗證失敗：" + e.getMessage()));
        }
    }

    /**
     * 忘記密碼：發送驗證碼
     * POST /api/auth/password/request-reset
     */
    @PostMapping("/password/request-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        try {
            String code = authService.requestPasswordReset(body.get("email"));
            // TODO: 正式環境移除 code，只回 message
            return ResponseEntity.ok(Map.of("message", "若此信箱已註冊，驗證碼已發送", "code", code != null ? code : ""));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "發送驗證碼失敗：" + e.getMessage()));
        }
    }

    /**
     * 忘記密碼：驗碼並重設密碼
     * POST /api/auth/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            authService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "密碼重設成功"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("RESET_FAILED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "密碼重設失敗"));
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