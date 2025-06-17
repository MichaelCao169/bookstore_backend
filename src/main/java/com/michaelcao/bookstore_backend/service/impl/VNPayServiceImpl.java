package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.config.VNPayConfig;
import com.michaelcao.bookstore_backend.dto.payment.VNPayCallbackRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentResponse;
import com.michaelcao.bookstore_backend.entity.Order;
import com.michaelcao.bookstore_backend.entity.OrderStatus;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.OrderRepository;
import com.michaelcao.bookstore_backend.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {
    
    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    
    @Override
    public VNPayPaymentResponse createPaymentUrl(UUID orderId, VNPayPaymentRequest request) {
        try {
            // Lấy thông tin đơn hàng
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));
            
            // Tạo transaction reference
            String txnRef = orderId.toString().replace("-", "");
            
            // Tạo parameters cho VNPay
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(order.getTotalAmount().multiply(new java.math.BigDecimal(100)).intValue())); // Convert to cents
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", request.getLocale());
            vnpParams.put("vnp_ReturnUrl", request.getReturnUrl());
            vnpParams.put("vnp_IpAddr", request.getIpAddress());
            
            // Tạo vnp_CreateDate
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            // Tạo vnp_ExpireDate (15 phút sau)
            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);
            
            // Sắp xếp parameters và tạo query string
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data (URL encoded for VNPay signature)
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    
                    // Build query (same as hash data for consistency)
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                }
            }
            
            // Tạo secure hash
            String vnpSecureHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);
            
            // Tạo payment URL
            String paymentUrl = vnPayConfig.getPayUrl() + "?" + query;
            
            log.info("Created VNPay payment URL for order {}", orderId);
            
            return VNPayPaymentResponse.success(paymentUrl, txnRef);
            
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for order {}: {}", orderId, e.getMessage(), e);
            return VNPayPaymentResponse.failure("Failed to create payment URL: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean handlePaymentCallback(VNPayCallbackRequest callback) {
        try {
            // Validate signature
            if (!validateCallback(callback)) {
                log.error("Invalid VNPay callback signature for transaction: {}", callback.getVnp_TxnRef());
                return false;
            }
            
            // Tìm đơn hàng từ transaction reference
            String orderIdStr = callback.getVnp_TxnRef();
            UUID orderId;
            try {
                // Reconstruct UUID from transaction reference
                String formattedOrderId = orderIdStr.substring(0, 8) + "-" +
                        orderIdStr.substring(8, 12) + "-" +
                        orderIdStr.substring(12, 16) + "-" +
                        orderIdStr.substring(16, 20) + "-" +
                        orderIdStr.substring(20, 32);
                orderId = UUID.fromString(formattedOrderId);
            } catch (Exception e) {
                log.error("Invalid order ID format in VNPay callback: {}", orderIdStr);
                return false;
            }
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));
            
            // Cập nhật trạng thái đơn hàng dựa trên kết quả thanh toán
            if (callback.isSuccessful()) {
                order.setStatus(OrderStatus.PAID);
                log.info("Payment successful for order {}, transaction: {}", orderId, callback.getVnp_TransactionNo());
                
                // Có thể thêm logic gửi email xác nhận, cập nhật inventory, etc.
                
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                log.warn("Payment failed for order {}, response code: {}", orderId, callback.getVnp_ResponseCode());
            }
            
            orderRepository.save(order);
            return true;
            
        } catch (Exception e) {
            log.error("Error handling VNPay callback: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String queryPaymentStatus(String transactionRef) {
        // Implementation for querying payment status from VNPay
        // This would typically involve making an API call to VNPay's query endpoint
        log.info("Querying payment status for transaction: {}", transactionRef);
        return "NOT_IMPLEMENTED";
    }
    
    @Override
    public boolean validateSignature(String params, String secureHash) {
        try {
            String calculatedHash = hmacSHA512(vnPayConfig.getSecretKey(), params);
            return calculatedHash.equals(secureHash);
        } catch (Exception e) {
            log.error("Error validating VNPay signature: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean validateCallback(VNPayCallbackRequest callback) {
        try {
            // Tạo lại hash data từ callback parameters
            Map<String, String> params = new HashMap<>();
            params.put("vnp_Amount", callback.getVnp_Amount());
            params.put("vnp_BankCode", callback.getVnp_BankCode());
            params.put("vnp_BankTranNo", callback.getVnp_BankTranNo());
            params.put("vnp_CardType", callback.getVnp_CardType());
            params.put("vnp_OrderInfo", callback.getVnp_OrderInfo());
            params.put("vnp_PayDate", callback.getVnp_PayDate());
            params.put("vnp_ResponseCode", callback.getVnp_ResponseCode());
            params.put("vnp_TmnCode", callback.getVnp_TmnCode());
            params.put("vnp_TransactionNo", callback.getVnp_TransactionNo());
            params.put("vnp_TransactionStatus", callback.getVnp_TransactionStatus());
            params.put("vnp_TxnRef", callback.getVnp_TxnRef());
            params.put("vnp_SecureHashType", callback.getVnp_SecureHashType());
            
            // Loại bỏ các parameters null
            params.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());
            
            // Sắp xếp và tạo hash data
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = params.get(fieldName);
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
            
            String calculatedHash = hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            return calculatedHash.equals(callback.getVnp_SecureHash());
            
        } catch (Exception e) {
            log.error("Error validating VNPay callback: {}", e.getMessage());
            return false;
        }
    }
    
    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] hashData = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hashData) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    

} 