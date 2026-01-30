package com.danceclub.club_system.config;

import com.danceclub.club_system.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security configuration
 * 需求：2.1, 2.6, 2.7, 2.8, 14.4, 14.5
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Authentication (需求：1.1, 1.2, 1.8, 1.9)
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/admin/login").permitAll()
                
                // Public endpoints - Activities (viewable by all) (需求：3.12, 3.13)
                .requestMatchers("/api/activities", "/api/activities/**").permitAll()
                
                // Admin endpoints - require ADMIN role (需求：2.3, 2.4, 2.5, 2.6, 2.7, 2.8)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // User profile endpoints - require authentication and proper authorization (需求：14.2, 14.3)
                .requestMatchers("/api/users/**").authenticated()
                
                // Registration endpoints - require authentication (需求：4.1, 4.10, 4.11, 5.1, 5.2)
                .requestMatchers("/api/registrations/**").authenticated()
                
                // Payment endpoints - require authentication (需求：6.7, 6.8, 7.1, 9.1)
                .requestMatchers("/api/payments/**").authenticated()
                
                // Leave endpoints - require authentication (需求：10.1, 10.7, 11.1)
                .requestMatchers("/api/leaves/**").authenticated()
                
                // Auth me endpoint - require authentication (需求：1.8)
                .requestMatchers("/api/auth/me").authenticated()
                
                // All other endpoints require authentication (需求：14.4, 14.5)
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    /**
     * Authentication entry point for handling unauthorized access
     * 需求：14.4, 14.5
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println("{ \"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\" }");
        };
    }

    /**
     * Access denied handler for handling forbidden access
     * 需求：2.6, 2.7, 2.8, 14.4, 14.5
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().println("{ \"error\": \"Access Denied\", \"message\": \"" + accessDeniedException.getMessage() + "\" }");
        };
    }

    /**
     * Authentication provider configuration
     * 需求：1.7, 1.8, 1.9
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager configuration
     * 需求：1.8, 1.9
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder using BCrypt
     * 需求：1.7, 14.1
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
