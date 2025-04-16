package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable; // Import Embeddable
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data; // Có thể dùng @Data cho Embeddable vì ít khi gây vấn đề vòng lặp
import lombok.NoArgsConstructor;

@Embeddable // Đánh dấu là một lớp có thể nhúng vào Entity khác
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255)
    @Column(name = "shipping_street", nullable = false) // Đặt tên cột rõ ràng
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100)
    @Column(name = "shipping_city", nullable = false)
    private String city;

    @NotBlank(message = "District cannot be blank")
    @Size(max = 100)
    @Column(name = "shipping_district", nullable = false)
    private String district; // Quận/Huyện

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100)
    @Column(name = "shipping_country", nullable = false)
    private String country;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 20)
    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String phone; // Số điện thoại liên lạc khi giao hàng

    // Có thể thêm các trường khác như Tên người nhận, Zip Code nếu cần
    @Size(max = 100)
    @Column(name = "recipient_name", length = 100)
    private String recipientName; // Tên người nhận hàng (có thể khác tên user)
}