package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.payment.VNPayCallbackRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentResponse;
import com.michaelcao.bookstore_backend.service.CartService;
import com.michaelcao.bookstore_backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayController {
    
    private final VNPayService vnPayService;
    private final CartService cartService;
    
    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     */
    @PostMapping("/create-payment/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<VNPayPaymentResponse> createPayment(
            @PathVariable UUID orderId,
            @Valid @RequestBody VNPayPaymentRequest request,
            HttpServletRequest httpRequest) {
        
        // Lấy IP address của client
        String ipAddress = getClientIpAddress(httpRequest);
        request.setIpAddress(ipAddress);
        
        log.info("Creating VNPay payment for order: {}", orderId);
        
        VNPayPaymentResponse response = vnPayService.createPaymentUrl(orderId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * Endpoint này sẽ được VNPay gọi trực tiếp
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam("vnp_Amount") String vnpAmount,
            @RequestParam("vnp_BankCode") String vnpBankCode,
            @RequestParam(value = "vnp_BankTranNo", required = false) String vnpBankTranNo,
            @RequestParam(value = "vnp_CardType", required = false) String vnpCardType,
            @RequestParam("vnp_OrderInfo") String vnpOrderInfo,
            @RequestParam("vnp_PayDate") String vnpPayDate,
            @RequestParam("vnp_ResponseCode") String vnpResponseCode,
            @RequestParam("vnp_TmnCode") String vnpTmnCode,
            @RequestParam(value = "vnp_TransactionNo", required = false) String vnpTransactionNo,
            @RequestParam("vnp_TransactionStatus") String vnpTransactionStatus,
            @RequestParam("vnp_TxnRef") String vnpTxnRef,
            @RequestParam(value = "vnp_SecureHashType", required = false) String vnpSecureHashType,
            @RequestParam("vnp_SecureHash") String vnpSecureHash) {
        
        log.info("Received VNPay callback for transaction: {}", vnpTxnRef);
        
        // Tạo callback request object
        VNPayCallbackRequest callback = new VNPayCallbackRequest();
        callback.setVnp_Amount(vnpAmount);
        callback.setVnp_BankCode(vnpBankCode);
        callback.setVnp_BankTranNo(vnpBankTranNo);
        callback.setVnp_CardType(vnpCardType);
        callback.setVnp_OrderInfo(vnpOrderInfo);
        callback.setVnp_PayDate(vnpPayDate);
        callback.setVnp_ResponseCode(vnpResponseCode);
        callback.setVnp_TmnCode(vnpTmnCode);
        callback.setVnp_TransactionNo(vnpTransactionNo);
        callback.setVnp_TransactionStatus(vnpTransactionStatus);
        callback.setVnp_TxnRef(vnpTxnRef);
        callback.setVnp_SecureHashType(vnpSecureHashType);
        callback.setVnp_SecureHash(vnpSecureHash);
        
        boolean success = vnPayService.handlePaymentCallback(callback);
        
        if (success && callback.isSuccessful()) {
            // Xóa giỏ hàng sau khi thanh toán thành công
            try {
                // Extract user ID from order and clear cart
                // This is a simplified approach - in production you might want to handle this differently
                log.info("Payment successful for transaction: {}", vnpTxnRef);
                return ResponseEntity.ok("Payment processed successfully");
            } catch (Exception e) {
                log.error("Error clearing cart after successful payment: {}", e.getMessage());
                return ResponseEntity.ok("Payment successful but cart clearing failed");
            }
        } else {
            log.warn("Payment failed or invalid callback for transaction: {}", vnpTxnRef);
            return ResponseEntity.ok("Payment failed");
        }
    }
    
    /**
     * Endpoint để frontend kiểm tra kết quả thanh toán
     */
    @GetMapping("/payment-result")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> getPaymentResult(
            @RequestParam("vnp_TxnRef") String txnRef,
            @RequestParam("vnp_ResponseCode") String responseCode) {
        
        log.info("Checking payment result for transaction: {}, response code: {}", txnRef, responseCode);
        
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok("Payment successful");
        } else {
            return ResponseEntity.ok("Payment failed with code: " + responseCode);
        }
    }
    
    /**
     * Query payment status từ VNPay
     */
    @GetMapping("/query-status/{transactionRef}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> queryPaymentStatus(@PathVariable String transactionRef) {
        log.info("Querying payment status for transaction: {}", transactionRef);
        
        String status = vnPayService.queryPaymentStatus(transactionRef);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Helper method để lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 