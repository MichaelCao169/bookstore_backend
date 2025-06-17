package com.michaelcao.bookstore_backend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VNPayPaymentRequest {
    
    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;
    
    @NotBlank(message = "Order info cannot be blank")
    private String orderInfo;
    
    @NotBlank(message = "Return URL cannot be blank")
    private String returnUrl;
    
    private String ipAddress;
    
    private String locale = "vn"; // Default locale
} 