package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.user.UpdateUserStatusRequest;
import com.michaelcao.bookstore_backend.dto.user.UserManagementDTO;
// Optional: import UpdateUserRolesRequest;
import com.michaelcao.bookstore_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users") // Base path cho API quản lý user của Admin
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Yêu cầu quyền ADMIN cho tất cả
public class AdminUserController {

    private final UserService userService;

    /**
     * Endpoint cho Admin lấy danh sách tất cả người dùng (có phân trang và tìm kiếm).
     */
    @GetMapping
    public ResponseEntity<Page<UserManagementDTO>> getAllUsers(
            @RequestParam(required = false) String keyword, // Tham số tìm kiếm (tùy chọn)
            @PageableDefault(size = 15, sort = "name") Pageable pageable) {
        log.info("Admin request: Get all users. Keyword: '{}', Pageable: {}", keyword, pageable);
        Page<UserManagementDTO> userPage = userService.getAllUsers(pageable, keyword);
        return ResponseEntity.ok(userPage);
    }

    /**
     * Endpoint cho Admin lấy chi tiết một người dùng theo ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserManagementDTO> getUserById(@PathVariable Long userId) {
        log.info("Admin request: Get user details for ID: {}", userId);
        UserManagementDTO userDTO = userService.getUserByIdForAdmin(userId);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Endpoint cho Admin cập nhật trạng thái (khóa/mở khóa) tài khoản người dùng.
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserManagementDTO> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("Admin request: Update status for user ID: {} to enabled={}", userId, request.getEnabled());
        UserManagementDTO updatedUser = userService.updateUserStatus(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * (Optional) Endpoint cho Admin cập nhật vai trò người dùng.
     */
    /*
    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserManagementDTO> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        log.info("Admin request: Update roles for user ID: {} to {}", userId, request.getRoleNames());
        UserManagementDTO updatedUser = userService.updateUserRoles(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
    */
}