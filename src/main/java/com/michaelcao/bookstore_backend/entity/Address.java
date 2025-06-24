package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable; // Import Embeddable
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data; // Có thể dùng @Data cho Embeddable vì ít khi gây vấn đề vòng lặp
import lombok.NoArgsConstructor;

@Embeddable // Đánh dấu là một lớp có thể nhúng vào Entity khác
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Size(max = 255)
    @Column(name = "shipping_street") // Đặt tên cột rõ ràng, cho phép null khi đăng ký
    private String street;

    @Size(max = 100)
    @Column(name = "shipping_city") // Cho phép null khi đăng ký
    private String city;

    @Size(max = 100)
    @Column(name = "shipping_district") // Cho phép null khi đăng ký
    private String district; // Quận/Huyện

    @Size(max = 100)
    @Column(name = "shipping_country") // Cho phép null khi đăng ký
    private String country;

    @Size(max = 20)
    @Column(name = "shipping_phone", length = 20) // Cho phép null khi đăng ký
    private String phone; // Số điện thoại liên lạc khi giao hàng

    // Có thể thêm các trường khác như Tên người nhận, Zip Code nếu cần
    @Size(max = 100)
    @Column(name = "recipient_name", length = 100)
    private String recipientName; // Tên người nhận hàng (có thể khác tên user)
}