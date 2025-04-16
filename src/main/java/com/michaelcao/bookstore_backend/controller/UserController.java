package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.user.ChangePasswordRequest;
import com.michaelcao.bookstore_backend.dto.user.UserProfileDTO;
import com.michaelcao.bookstore_backend.entity.User; // Import User
import com.michaelcao.bookstore_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile") // Base path cho các API liên quan đến profile người dùng
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()") // Yêu cầu xác thực cho tất cả các endpoint profile
public class UserController {

    private final UserService userService;

    // Helper method để lấy User ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            return currentUser.getId();
        }
        log.error("Could not get current user ID from Security Context for profile action.");
        throw new IllegalStateException("User not authenticated properly."); // Ném lỗi nếu không lấy được ID
    }

    /**
     * Endpoint để lấy thông tin profile của người dùng đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        log.info("Request received to get profile for user ID: {}", userId);
        UserProfileDTO userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Endpoint để thay đổi mật khẩu của người dùng đang đăng nhập.
     */
    @PutMapping("/change-password")
    public ResponseEntity<String> changeCurrentUserPassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        log.info("Request received to change password for user ID: {}", userId);
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (IllegalArgumentException e) { // Bắt lỗi mật khẩu cũ sai hoặc mật khẩu mới trùng
            log.warn("Password change failed for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        // Các lỗi khác (ResourceNotFound) sẽ được GlobalExceptionHandler xử lý
    }

    /**
     * (Optional) Endpoint để cập nhật thông tin profile (ví dụ: tên).
     */
    /*
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        log.info("Request received to update profile for user ID: {}", userId);
        UserProfileDTO updatedProfile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }
    */

}