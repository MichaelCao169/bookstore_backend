package com.michaelcao.bookstore_backend.dto.user;

import com.michaelcao.bookstore_backend.dto.address.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
    private String phone; // Số điện thoại
    private AddressDTO defaultAddress; // Địa chỉ mặc định
    // Không bao gồm password hay roles ở đây
}