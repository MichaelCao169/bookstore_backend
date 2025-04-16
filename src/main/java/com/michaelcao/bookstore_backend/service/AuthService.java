package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;
import org.springframework.http.ResponseEntity; // Keep using ResponseEntity for now

public interface AuthService {

    // Return a simple message for now, actual response later
    ResponseEntity<String> register(RegisterRequest registerRequest);

    // Will return AuthResponse wrapped in ResponseEntity later
    ResponseEntity<?> login(LoginRequest loginRequest);

    // Add method signatures for other auth features later
     void verifyAccount(String token);
     // void forgotPassword(String email);
    // void resetPassword(String token, String newPassword);
     void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
    // AuthResponse refreshToken(String refreshToken);
}