package com.michaelcao.bookstore_backend.dto.order;

import com.michaelcao.bookstore_backend.entity.PaymentMethod; // Import Enum
import jakarta.validation.Valid; // Import Valid để validate Address lồng vào
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO này chứa thông tin cần thiết để tạo đơn hàng từ phía client
// Giả định giỏ hàng được lấy từ user đang đăng nhập, không cần gửi ID giỏ hàng
@Data
public class CreateOrderRequest {

    // Thông tin địa chỉ giao hàng (Lồng vào và validate)
    @NotNull(message = "Shipping address cannot be null")
    @Valid // *** QUAN TRỌNG: Validate các trường bên trong Address ***
    private AddressInfo shippingAddress;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod; // Client gửi lên phương thức thanh toán họ chọn

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes; // Ghi chú (tùy chọn)

    // --- Lớp nội bộ (inner class) hoặc DTO riêng cho Address ---
    // Sử dụng lớp nội bộ ở đây cho tiện, hoặc tạo file AddressInfo.java riêng
    @Data
    public static class AddressInfo {
        @NotBlank(message = "Street address cannot be blank")
        @Size(max = 255)
        private String street;

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100)
        private String city;

        @NotBlank(message = "District cannot be blank")
        @Size(max = 100)
        private String district;

        @NotBlank(message = "Country cannot be blank")
        @Size(max = 100)
        private String country;

        @NotBlank(message = "Phone number cannot be blank")
        @Size(max = 20)
        private String phone;

        @Size(max = 100)
        private String recipientName; // Tên người nhận, có thể null nếu giống tên user
    }
}