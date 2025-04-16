package com.michaelcao.bookstore_backend.entity; // Hoặc com.michaelcao.bookstore_backend.enums;

// *** ĐẶT LÀ PUBLIC ***
public enum PaymentMethod {
    COD,              // Thanh toán khi nhận hàng
    VNPAY,            // Thanh toán qua VNPay
    BANK_TRANSFER,    // Chuyển khoản ngân hàng (ví dụ)
    OTHER             // Khác
}