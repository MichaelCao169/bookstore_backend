package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import com.michaelcao.bookstore_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Processing registration request for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        log.info("Processing logout request");
        authService.logout();
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        log.info("Processing email verification request");
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken() {
        log.info("Processing token refresh request");
        return ResponseEntity.ok(authService.refreshToken());
    }
}