package com.michaelcao.bookstore_backend.dto.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private String street;
    private String city;
    private String district;
    private String country;
    private String phone;
    private String recipientName; // Thêm tên người nhận
}