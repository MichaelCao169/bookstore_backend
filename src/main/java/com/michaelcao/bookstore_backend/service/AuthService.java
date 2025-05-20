package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.auth.AuthResponse;
import com.michaelcao.bookstore_backend.dto.auth.LoginRequest;
import com.michaelcao.bookstore_backend.dto.auth.RegisterRequest;

public interface AuthService {
    
    /**
     * Register a new user
     * @param request Registration details
     * @return Authentication response with token
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticate a user
     * @param request Login credentials
     * @return Authentication response with token
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Log out the current user
     */
    void logout();
    
    /**
     * Verify a user's email with token
     * @param token Email verification token
     */
    void verifyEmail(String token);
    
    /**
     * Refresh the authentication token
     * @return New authentication response with refreshed token
     */
    AuthResponse refreshToken();

    /**
     * Process a forgot password request
     * @param email The email address of the user requesting password reset
     */
    void forgotPassword(String email);

    /**
     * Reset a user's password using a valid reset token
     * @param token The password reset token
     * @param newPassword The new password to set
     */
    void resetPassword(String token, String newPassword);
}