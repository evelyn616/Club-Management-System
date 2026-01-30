package com.danceclub.club_system;

import com.danceclub.club_system.dto.JwtResponse;
import com.danceclub.club_system.dto.LoginRequest;
import com.danceclub.club_system.dto.RegisterRequest;
import com.danceclub.club_system.repository.UserRepository;
import com.danceclub.club_system.security.JwtTokenProvider;
import com.danceclub.club_system.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Token Integration Test
 * 測試 JWT token 的生成、驗證和使用流程
 */
@SpringBootTest
@Transactional
class JwtTokenTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest testRegisterRequest;
    private LoginRequest testLoginRequest;

    @BeforeEach
    void setUp() {
        // 使用唯一的 email 來避免與現有資料衝突
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";

        // 準備測試用的註冊請求
        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setName("測試用戶");
        testRegisterRequest.setEmail(uniqueEmail);
        testRegisterRequest.setPassword("Test123456");
        testRegisterRequest.setPhone("0912345678");
        testRegisterRequest.setSchool("測試大學");
        testRegisterRequest.setBirthday(LocalDate.of(2000, 1, 1));

        // 準備測試用的登入請求
        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail(uniqueEmail);
        testLoginRequest.setPassword("Test123456");
    }

    @Test
    void testUserRegistrationGeneratesValidToken() {
        // 測試用戶註冊並生成 JWT token
        JwtResponse response = authService.register(testRegisterRequest);

        // 驗證回應不為空
        assertNotNull(response, "JWT response should not be null");
        assertNotNull(response.getToken(), "JWT token should not be null");
        assertNotNull(response.getUser(), "User response should not be null");
        assertEquals("Bearer", response.getType(), "Token type should be Bearer");

        // 驗證用戶資訊
        assertEquals("測試用戶", response.getUser().getName());
        assertEquals(testRegisterRequest.getEmail(), response.getUser().getEmail());
        assertEquals("member", response.getUser().getRole());

        // 驗證 token 是否有效
        String token = response.getToken();
        String extractedEmail = jwtTokenProvider.extractUsername(token);
        assertEquals(testRegisterRequest.getEmail(), extractedEmail, "Extracted email should match");

        // 驗證 token 對該用戶是否有效
        UserDetails userDetails = userDetailsService.loadUserByUsername(testRegisterRequest.getEmail());
        assertTrue(jwtTokenProvider.isTokenValid(token, userDetails), "Token should be valid for the user");

        System.out.println("✓ 註冊成功並生成有效的 JWT token");
        System.out.println("  Token: " + token.substring(0, Math.min(50, token.length())) + "...");
        System.out.println("  User ID: " + response.getUser().getId());
        System.out.println("  Email: " + response.getUser().getEmail());
    }

    @Test
    void testUserLoginGeneratesValidToken() {
        // 先註冊用戶
        authService.register(testRegisterRequest);

        // 測試用戶登入並生成 JWT token
        JwtResponse response = authService.login(testLoginRequest);

        // 驗證回應不為空
        assertNotNull(response, "JWT response should not be null");
        assertNotNull(response.getToken(), "JWT token should not be null");
        assertNotNull(response.getUser(), "User response should not be null");

        // 驗證用戶資訊
        assertEquals("測試用戶", response.getUser().getName());
        assertEquals(testRegisterRequest.getEmail(), response.getUser().getEmail());

        // 驗證 token 是否有效
        String token = response.getToken();
        UserDetails userDetails = userDetailsService.loadUserByUsername(testRegisterRequest.getEmail());
        assertTrue(jwtTokenProvider.isTokenValid(token, userDetails), "Token should be valid for the user");

        System.out.println("✓ 登入成功並生成有效的 JWT token");
        System.out.println("  Token: " + token.substring(0, Math.min(50, token.length())) + "...");
    }

    @Test
    void testTokenExtractionAndValidation() {
        // 註冊用戶並獲取 token
        JwtResponse response = authService.register(testRegisterRequest);
        String token = response.getToken();

        // 測試從 token 中提取用戶名
        String extractedEmail = jwtTokenProvider.extractUsername(token);
        assertEquals(testRegisterRequest.getEmail(), extractedEmail, "Should extract correct email from token");

        // 測試 token 驗證
        UserDetails userDetails = userDetailsService.loadUserByUsername(extractedEmail);
        assertTrue(jwtTokenProvider.isTokenValid(token, userDetails), "Token should be valid");

        System.out.println("✓ Token 提取和驗證成功");
        System.out.println("  Extracted Email: " + extractedEmail);
    }

    @Test
    void testInvalidTokenValidation() {
        // 註冊用戶
        authService.register(testRegisterRequest);

        // 創建一個無效的 token
        String invalidToken = "invalid.token.here";

        // 測試無效 token 應該拋出異常
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.extractUsername(invalidToken);
        }, "Invalid token should throw exception");

        System.out.println("✓ 無效 token 正確被拒絕");
    }

    @Test
    void testAdminLoginWithMemberAccount() {
        // 註冊普通會員
        authService.register(testRegisterRequest);

        // 嘗試用普通會員帳號進行管理員登入
        assertThrows(IllegalArgumentException.class, () -> {
            authService.adminLogin(testLoginRequest);
        }, "Member account should not be able to login as admin");

        System.out.println("✓ 普通會員無法使用管理員登入");
    }

    @Test
    void testDuplicateEmailRegistration() {
        // 第一次註冊
        authService.register(testRegisterRequest);

        // 嘗試用相同 email 再次註冊
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(testRegisterRequest);
        }, "Should not allow duplicate email registration");

        System.out.println("✓ 重複 email 註冊被正確拒絕");
    }

    @Test
    void testInvalidEmailFormat() {
        // 測試無效的 email 格式
        testRegisterRequest.setEmail("invalid-email");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(testRegisterRequest);
        }, "Should reject invalid email format");

        System.out.println("✓ 無效 email 格式被正確拒絕");
    }

    @Test
    void testLoginWithWrongPassword() {
        // 先註冊用戶
        authService.register(testRegisterRequest);

        // 使用錯誤密碼登入
        testLoginRequest.setPassword("WrongPassword123");

        assertThrows(Exception.class, () -> {
            authService.login(testLoginRequest);
        }, "Should reject login with wrong password");

        System.out.println("✓ 錯誤密碼登入被正確拒絕");
    }

    @Test
    void testGetCurrentUser() {
        // 註冊用戶
        JwtResponse registerResponse = authService.register(testRegisterRequest);

        // 使用 email 獲取當前用戶資訊
        var userResponse = authService.getCurrentUser(testRegisterRequest.getEmail());

        assertNotNull(userResponse, "User response should not be null");
        assertEquals("測試用戶", userResponse.getName());
        assertEquals(testRegisterRequest.getEmail(), userResponse.getEmail());
        assertEquals("member", userResponse.getRole());

        System.out.println("✓ 成功獲取當前用戶資訊");
        System.out.println("  User ID: " + userResponse.getId());
        System.out.println("  Name: " + userResponse.getName());
        System.out.println("  Role: " + userResponse.getRole());
    }
}
