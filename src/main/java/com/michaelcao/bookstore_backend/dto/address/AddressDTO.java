package com.michaelcao.bookstore_backend.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "District cannot be blank")
    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district; // Quận/Huyện

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone; // Số điện thoại liên lạc khi giao hàng

    @Size(max = 100, message = "Recipient name cannot exceed 100 characters")
    private String recipientName; // Tên người nhận hàng (có thể khác tên user)
}