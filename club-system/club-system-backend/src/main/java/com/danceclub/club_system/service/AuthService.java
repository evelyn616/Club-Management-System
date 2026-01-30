package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.JwtResponse;
import com.danceclub.club_system.dto.LoginRequest;
import com.danceclub.club_system.dto.RegisterRequest;
import com.danceclub.club_system.dto.UserResponse;
import com.danceclub.club_system.model.User;
import com.danceclub.club_system.repository.UserRepository;
import com.danceclub.club_system.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Service for handling user authentication operations
 * 需求：1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final IdGeneratorService idGeneratorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public AuthService(UserRepository userRepository,
                      IdGeneratorService idGeneratorService,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider,
                      AuthenticationManager authenticationManager,
                      UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.idGeneratorService = idGeneratorService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Register a new user
     * 需求：1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.10
     */
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("電子郵件格式無效");
        }
        
        // Check email uniqueness
        if (isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("此電子郵件已被註冊");
        }
        
        // Generate unique member ID
        String memberId = idGeneratorService.generateMemberId();
        
        // Create new user entity
        User user = new User();
        user.setId(memberId);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Encrypt password with bcrypt
        user.setPhone(request.getPhone());
        user.setSchool(request.getSchool());
        user.setBirthday(request.getBirthday());
        user.setRole("member"); // Default role is member
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);
        
        // Convert to response DTO
        UserResponse userResponse = convertToUserResponse(savedUser);
        
        return new JwtResponse(token, userResponse);
    }
    
    /**
     * Member login (for member login interface)
     * 需求：1.8, 1.9
     */
    public JwtResponse login(LoginRequest request) {
        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("電子郵件格式無效");
        }
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        
        // Convert to response DTO
        UserResponse userResponse = convertToUserResponse(user);
        
        return new JwtResponse(token, userResponse);
    }
    
    /**
     * Admin login (for admin backend interface)
     * 需求：1.9
     */
    public JwtResponse adminLogin(LoginRequest request) {
        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("電子郵件格式無效");
        }
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        // Check if user is admin
        if (!"admin".equals(user.getRole())) {
            throw new IllegalArgumentException("權限不足，僅限管理員登入");
        }
        
        // Generate JWT token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        
        // Convert to response DTO
        UserResponse userResponse = convertToUserResponse(user);
        
        return new JwtResponse(token, userResponse);
    }
    
    /**
     * Get current user information
     * 需求：1.8
     */
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        return convertToUserResponse(user);
    }
    
    /**
     * Validate email format
     * 需求：1.10
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Check if email already exists
     * 需求：1.10
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
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