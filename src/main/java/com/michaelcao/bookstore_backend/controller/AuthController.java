package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import com.michaelcao.bookstore_backend.dto.auth.ForgotPasswordRequest;
import com.michaelcao.bookstore_backend.dto.auth.ResetPasswordRequest;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    
    @Value("${app.jwt.refresh-cookie-name:bookstore_refresh_token}")
    private String refreshTokenCookieName;
    
    @Value("${jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        log.info("Processing login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        
        // Kiểm tra nếu đăng nhập thành công
        if (authResponse.getVerified() == null || !authResponse.getVerified()) {
            // Đăng nhập thất bại - trả về 401 Unauthorized với thông báo lỗi
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
        
        // Đặt refresh token làm cookie HttpOnly nếu đăng nhập thành công
        if (authResponse.getRefreshToken() != null) {
            Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Đặt thành true trong nếu thực tế với HTTPS
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) (refreshTokenExpirationMs / 1000)); // Chuyển ms sang giây
            response.addCookie(refreshTokenCookie);
            
            log.info("Set refresh token cookie: name='{}', path='/', maxAge={}, httpOnly=true", 
                refreshTokenCookieName, refreshTokenCookie.getMaxAge());
            
            // Xóa refresh token khỏi body response cho bảo mật
            authResponse.setRefreshToken(null);
        }
        
        return ResponseEntity.ok(authResponse);
    }@PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("Processing logout request");
        authService.logout();
        
        // Xóa refresh token cookie
        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Đặt thành true trong nếu thực tế với HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Xóa cookie
        response.addCookie(refreshTokenCookie);
        
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        log.info("Processing email verification request for token: {}", token.substring(0, Math.min(8, token.length())) + "...");
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok("Email verified successfully");
        } catch (ResourceNotFoundException e) {
            log.warn("Email verification failed - token not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Token not found or already used. If you just verified your email, please try logging in.");
        } catch (IllegalStateException e) {
            log.warn("Email verification failed - token expired: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Token has expired. Please request a new verification email.");
        } catch (Exception e) {
            log.error("Unexpected error during email verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during email verification. Please try again later.");
        }
    }    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        log.info("Processing token refresh request");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("If an account exists with this email, you will receive a password reset link.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset request");
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully");
    }
}