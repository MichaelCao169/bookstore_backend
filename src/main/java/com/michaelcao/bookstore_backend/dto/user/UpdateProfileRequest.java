package com.michaelcao.bookstore_backend.dto.user;

import com.michaelcao.bookstore_backend.dto.address.AddressDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    // Thêm các trường mới
    private String displayName; // Tên hiển thị
    private String phone; // Số điện thoại
    
    // Địa chỉ mặc định
    private AddressDTO defaultAddress;
    
    // Không cho phép sửa email ở đây
}