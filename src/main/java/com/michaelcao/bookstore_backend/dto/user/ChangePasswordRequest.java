package com.michaelcao.bookstore_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;

    // Optional: Thêm trường confirmNewPassword và validation tùy chỉnh nếu muốn kiểm tra phía backend
    // private String confirmNewPassword;
}