package com.michaelcao.bookstore_backend.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    @NotEmpty(message = "Roles set cannot be empty")
    private Set<String> roleNames; // Danh sách tên các role mới muốn gán (ví dụ: ["ROLE_CUSTOMER", "ROLE_ADMIN"])
}