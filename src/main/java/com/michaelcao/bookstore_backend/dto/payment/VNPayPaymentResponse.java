package com.michaelcao.bookstore_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayPaymentResponse {
    
    private String paymentUrl;
    private String transactionRef;
    private String message;
    private boolean success;
    
    public static VNPayPaymentResponse success(String paymentUrl, String transactionRef) {
        return new VNPayPaymentResponse(paymentUrl, transactionRef, "Payment URL created successfully", true);
    }
    
    public static VNPayPaymentResponse failure(String message) {
        return new VNPayPaymentResponse(null, null, message, false);
    }
} 