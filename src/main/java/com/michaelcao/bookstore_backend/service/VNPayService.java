package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.payment.VNPayCallbackRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentResponse;

import java.util.UUID;

public interface VNPayService {
    
    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * @param orderId ID của đơn hàng
     * @param request Thông tin thanh toán
     * @return Response chứa payment URL
     */
    VNPayPaymentResponse createPaymentUrl(UUID orderId, VNPayPaymentRequest request);
    
    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * @param callback Thông tin callback từ VNPay
     * @return True nếu thanh toán thành công, False nếu thất bại
     */
    boolean handlePaymentCallback(VNPayCallbackRequest callback);
    
    /**
     * Kiểm tra trạng thái thanh toán từ VNPay
     * @param transactionRef Reference của transaction
     * @return Trạng thái thanh toán
     */
    String queryPaymentStatus(String transactionRef);
    
    /**
     * Validate signature từ VNPay
     * @param params Tham số từ VNPay
     * @param secureHash Secure hash từ VNPay
     * @return True nếu signature hợp lệ
     */
    boolean validateSignature(String params, String secureHash);
} 