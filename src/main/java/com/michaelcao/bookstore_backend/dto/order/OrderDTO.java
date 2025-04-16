package com.michaelcao.bookstore_backend.dto.order;

import com.michaelcao.bookstore_backend.entity.OrderStatus; // Import Enum
import com.michaelcao.bookstore_backend.entity.PaymentMethod; // Import Enum
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList; // Import ArrayList
import java.util.List;    // Import List

@Data
@NoArgsConstructor
public class OrderDTO {
    private Long orderId; // ID của đơn hàng
    private Long userId; // ID của người đặt hàng
    private String userName; // Tên người đặt hàng (lấy từ User)
    private String userEmail; // Email người đặt hàng (lấy từ User)
    private Instant orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String notes;

    // --- Thông tin địa chỉ giao hàng (Nhúng trực tiếp các trường) ---
    private String shippingStreet;
    private String shippingCity;
    private String shippingDistrict;
    private String shippingCountry;
    private String shippingPhone;
    private String shippingRecipientName; // Tên người nhận hàng

    // --- Danh sách các mặt hàng trong đơn ---
    private List<OrderItemDTO> orderItems = new ArrayList<>();

}