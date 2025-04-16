package com.michaelcao.bookstore_backend.entity; // Hoặc com.michaelcao.bookstore_backend.enums;

// *** ĐẶT LÀ PUBLIC ***
public enum OrderStatus {
    PENDING,          // Mới tạo, chờ xử lý (COD) hoặc chờ thanh toán (Online)
    PENDING_PAYMENT,  // Chờ thanh toán (Online)
    PROCESSING,       // Đã xác nhận/thanh toán, đang chuẩn bị hàng
    SHIPPED,          // Đã giao cho đơn vị vận chuyển
    DELIVERED,        // Đã giao thành công
    CANCELLED,        // Đã hủy
    PAYMENT_FAILED    // Thanh toán online thất bại
}