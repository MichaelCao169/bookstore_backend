package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import com.michaelcao.bookstore_backend.entity.Role;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.entity.VerificationToken;
import com.michaelcao.bookstore_backend.entity.PasswordResetToken;
import com.michaelcao.bookstore_backend.entity.RefreshToken;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.exception.TokenRefreshException;
import com.michaelcao.bookstore_backend.repository.RoleRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.repository.VerificationTokenRepository;
import com.michaelcao.bookstore_backend.repository.PasswordResetTokenRepository;
import com.michaelcao.bookstore_backend.security.jwt.JwtUtil;
import com.michaelcao.bookstore_backend.service.AuthService;
import com.michaelcao.bookstore_backend.service.EmailService;
import com.michaelcao.bookstore_backend.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;    
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.jwt.refresh-cookie-name:bookstore_refresh_token}")
    private String refreshTokenCookieName;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        // Get default CUSTOMER role
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Error: Role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

        // Create new user with enabled=false (needs verification)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(false) // Set to false now that email verification is required
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Create verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        tokenRepository.save(verificationToken);
        
        // Send verification email
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verificationUrl);
        
        // Return auth response (user will still need to verify email)
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .roles(savedUser.getAuthorities().stream()
                        .map(role -> role.getAuthority())
                        .collect(Collectors.toList()))
                .verified(false)
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = (User) authentication.getPrincipal();
            log.info("User authenticated: {}", user.getEmail());

            // Generate JWT token
            String token = jwtUtil.generateToken(user);
            
            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Return auth response (refresh token will be set as cookie in controller layer if needed)
            return AuthResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .avatarUrl(user.getAvatarUrl())
                    .roles(user.getAuthorities().stream()
                            .map(role -> role.getAuthority())
                            .collect(Collectors.toList()))
                    .accessToken(token)
                    .refreshToken(refreshToken.getToken()) // Include for cookie setting
                    .verified(true)
                    .build();
        } catch (DisabledException e) {
            log.warn("Login attempt for disabled account: {}", request.getEmail());
            return AuthResponse.builder()
                    .verified(false)
                    .message("Tài khoản cần được xác minh. Vui lòng kiểm tra email của bạn.")
                    .build();
        } catch (Exception e) {
            log.warn("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            return AuthResponse.builder()
                    .verified(false)
                    .message("Email hoặc mật khẩu không đúng")
                    .build();
        }
    }

    @Override
    public void logout() {
        // Clear the security context
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // Find token in database
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "value", token));
        
        // Check if token is expired
        if (verificationToken.isExpired()) {
            throw new IllegalStateException("Token is expired");
        }
        
        // Enable user
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        
        // Token is used only once
        tokenRepository.delete(verificationToken);
        
        log.info("Email verified successfully for user: {}", user.getEmail());
    }    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request);
        
        if (refreshToken == null) {
            throw new TokenRefreshException("Refresh token is missing from cookies");
        }

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Generate new access token
                    String newAccessToken = jwtUtil.generateToken(user);
                    
                    log.info("Token refreshed for user: {}", user.getEmail());
                    
                    // Return auth response with new access token
                    return AuthResponse.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .avatarUrl(user.getAvatarUrl())
                            .roles(user.getAuthorities().stream()
                                    .map(role -> role.getAuthority())
                                    .collect(Collectors.toList()))
                            .accessToken(newAccessToken)
                            .verified(true)
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token is not valid"));
    }    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        log.debug("Looking for refresh token cookie with name: {}", refreshTokenCookieName);
        if (request.getCookies() != null) {
            log.debug("Found {} cookies in request", request.getCookies().length);
            for (Cookie cookie : request.getCookies()) {
                log.debug("Cookie: name='{}', value='{}', path='{}'", 
                    cookie.getName(), 
                    cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "..." : "null",
                    cookie.getPath());
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    log.debug("Found matching refresh token cookie");
                    return cookie.getValue();
                }
            }
            log.debug("No matching refresh token cookie found");
        } else {
            log.debug("No cookies found in request");
        }
        return null;
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null); // Don't throw exception to prevent email enumeration
        
        if (user != null) {
            // Delete any existing password reset tokens for this user
            passwordResetTokenRepository.deleteByUser(user);
            
            // Create new password reset token
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(passwordResetToken);
            
            // Generate reset URL
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetUrl);
            log.info("Password reset email sent to: {}", email);
        } else {
            log.warn("Forgot password request for non-existent email: {}", email);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Processing password reset request with token");
        
        // Find token in database
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "value", token));
        
        // Check if token is expired
        if (resetToken.isExpired()) {
            throw new IllegalStateException("Token is expired");
        }
        
        // Get user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Delete used token
        passwordResetTokenRepository.delete(resetToken);
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
} 