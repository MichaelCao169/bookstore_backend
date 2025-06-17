package com.michaelcao.bookstore_backend.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VNPayCallbackRequest {
    
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_BankTranNo;
    private String vnp_CardType;
    private String vnp_OrderInfo;
    private String vnp_PayDate;
    private String vnp_ResponseCode;
    private String vnp_TmnCode;
    private String vnp_TransactionNo;
    private String vnp_TransactionStatus;
    private String vnp_TxnRef;
    private String vnp_SecureHashType;
    private String vnp_SecureHash;
    
    // Convenience methods
    public BigDecimal getAmountInVND() {
        if (vnp_Amount != null) {
            try {
                return new BigDecimal(vnp_Amount).divide(new BigDecimal(100)); // VNPay returns amount in cents
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    public boolean isSuccessful() {
        return "00".equals(vnp_ResponseCode);
    }
} 