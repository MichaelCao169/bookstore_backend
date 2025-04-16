package com.michaelcao.bookstore_backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "Enabled status cannot be null")
    private Boolean enabled; // Trạng thái mới: true (mở khóa) hoặc false (khóa)
}