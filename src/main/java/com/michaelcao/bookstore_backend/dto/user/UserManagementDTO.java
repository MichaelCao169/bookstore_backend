package com.michaelcao.bookstore_backend.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // Import List
import java.util.Set; // Import Set

@Data
@NoArgsConstructor
public class UserManagementDTO {
    private Long id;
    private String name;
    private String email;
    private boolean enabled; // Trạng thái tài khoản (đã kích hoạt/khóa)
    private Set<String> roles; // Danh sách tên các vai trò (ví dụ: ["ROLE_CUSTOMER", "ROLE_ADMIN"])

    // Constructor (tùy chọn)
    public UserManagementDTO(Long id, String name, String email, boolean enabled, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }
}