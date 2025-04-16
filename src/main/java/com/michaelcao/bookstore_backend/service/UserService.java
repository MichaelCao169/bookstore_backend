package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.user.ChangePasswordRequest; // Sẽ tạo DTO này
import com.michaelcao.bookstore_backend.dto.user.UserProfileDTO;      // Sẽ tạo DTO này
// Optional: import UpdateProfileRequest
import com.michaelcao.bookstore_backend.dto.user.UserManagementDTO; // Import DTO mới
import com.michaelcao.bookstore_backend.dto.user.UpdateUserStatusRequest; // Import DTO mới
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
public interface UserService {

    /**
     * Lấy thông tin profile của người dùng đang đăng nhập.
     * @param userId ID của người dùng đang đăng nhập.
     * @return UserProfileDTO chứa thông tin public của user.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy user.
     */
    UserProfileDTO getUserProfile(Long userId);

    /**
     * Thay đổi mật khẩu cho người dùng đang đăng nhập.
     * @param userId ID của người dùng đang đăng nhập.
     * @param request DTO chứa mật khẩu cũ và mật khẩu mới.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy user.
     * @throws IllegalArgumentException Nếu mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ.
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * (Optional) Cập nhật thông tin profile cơ bản (ví dụ: tên).
     * @param userId ID của người dùng.
     * @param request DTO chứa thông tin cập nhật.
     * @return UserProfileDTO sau khi cập nhật.
     */
    // UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);

    // --- Admin Methods ---

    /**
     * Lấy danh sách tất cả người dùng (có phân trang, có thể lọc/tìm kiếm).
     * @param pageable Thông tin phân trang và sắp xếp.
     * @param keyword (Optional) Từ khóa tìm kiếm theo tên hoặc email.
     * @return Page chứa danh sách UserManagementDTO.
     */
    Page<UserManagementDTO> getAllUsers(Pageable pageable, String keyword); // Thêm keyword

    /**
     * Lấy thông tin chi tiết của một người dùng bất kỳ (dành cho Admin).
     * @param userId ID của người dùng.
     * @return UserManagementDTO chi tiết.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy user.
     */
    UserManagementDTO getUserByIdForAdmin(Long userId); // Đổi tên cho rõ ràng

    /**
     * Cập nhật trạng thái (enabled/disabled) của một người dùng.
     * @param userId ID của người dùng cần cập nhật.
     * @param request DTO chứa trạng thái mới (enabled = true/false).
     * @return UserManagementDTO sau khi cập nhật.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy user.
     */
    UserManagementDTO updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /**
     * (Optional) Cập nhật vai trò cho người dùng.
     * @param userId ID của người dùng.
     * @param request DTO chứa danh sách tên các vai trò mới.
     * @return UserManagementDTO sau khi cập nhật.
     */
    // UserManagementDTO updateUserRoles(Long userId, UpdateUserRolesRequest request);

}