package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.order.CreateOrderRequest;
import com.michaelcao.bookstore_backend.dto.order.OrderDTO;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentRequest;
import com.michaelcao.bookstore_backend.dto.payment.VNPayPaymentResponse;
import com.michaelcao.bookstore_backend.entity.PaymentMethod;
import com.michaelcao.bookstore_backend.entity.User; // Import User
import com.michaelcao.bookstore_backend.service.OrderService;
import com.michaelcao.bookstore_backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault; // Import PageableDefault
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders") // Base path cho các API liên quan đến đơn hàng của người dùng
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')") // Yêu cầu quyền CUSTOMER cho tất cả API trong controller này
public class OrderController {

    private final OrderService orderService;
    private final VNPayService vnPayService;

    // Helper method để lấy User ID từ Security Context (Giống CartController)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            log.error("Could not retrieve authenticated user information for order operation.");
            throw new IllegalStateException("User not authenticated properly");
        }
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId();
    }

    /**
     * Endpoint để tạo một đơn hàng mới từ giỏ hàng của người dùng hiện tại.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId();
        log.info("Request received to create order for user ID: {} with payment method: {}", userId, request.getPaymentMethod());
        
        // Tạo đơn hàng
        OrderDTO createdOrder = orderService.createOrder(userId, request);
        
        // Nếu là thanh toán VNPay, tạo payment URL
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            return handleVNPayPayment(createdOrder, httpRequest);
        }
        
        // Trả về đơn hàng cho các phương thức thanh toán khác
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Endpoint để lấy lịch sử đơn hàng của người dùng hiện tại (phân trang).
     */
    @GetMapping("/my-history")
    public ResponseEntity<Page<OrderDTO>> getMyOrderHistory(
            @PageableDefault(size = 10, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Long userId = getCurrentUserId();
        log.info("Request received to get order history for user ID: {} with pagination: {}", userId, pageable);
        Page<OrderDTO> orderPage = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orderPage);
    }

    /**
     * Endpoint để lấy chi tiết một đơn hàng cụ thể của người dùng hiện tại.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getMyOrderDetails(@PathVariable UUID orderId) {
        Long userId = getCurrentUserId();
        log.info("Request received to get details for order ID: {} for user ID: {}", orderId, userId);
        OrderDTO orderDetails = orderService.getOrderDetails(userId, orderId);
        return ResponseEntity.ok(orderDetails);
    }

    /**
     * Endpoint để khách hàng hủy đơn hàng của mình.
     * Chỉ cho phép hủy đơn hàng ở trạng thái PENDING hoặc PENDING_PAYMENT.
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable UUID orderId) {
        Long userId = getCurrentUserId();
        log.info("Request received to cancel order ID: {} for user ID: {}", orderId, userId);
        OrderDTO cancelledOrder = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * Helper method để xử lý thanh toán VNPay
     */
    private ResponseEntity<?> handleVNPayPayment(OrderDTO order, HttpServletRequest httpRequest) {
        try {
            // Tạo VNPay payment request
            VNPayPaymentRequest paymentRequest = new VNPayPaymentRequest();
            paymentRequest.setAmount(order.getTotalAmount());
            paymentRequest.setOrderInfo("Thanh toan don hang " + order.getOrderId().toString().substring(0, 8));
            paymentRequest.setReturnUrl("http://localhost:3000/payment/result"); // Frontend URL
            
            // Lấy IP address của client
            String ipAddress = getClientIpAddress(httpRequest);
            paymentRequest.setIpAddress(ipAddress);
            
            // Tạo payment URL
            VNPayPaymentResponse paymentResponse = vnPayService.createPaymentUrl(order.getOrderId(), paymentRequest);
            
            if (paymentResponse.isSuccess()) {
                return ResponseEntity.ok(paymentResponse);
            } else {
                return ResponseEntity.badRequest().body(paymentResponse);
            }
            
        } catch (Exception e) {
            log.error("Error creating VNPay payment for order {}: {}", order.getOrderId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to create payment URL");
        }
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
        
        String remoteAddr = request.getRemoteAddr();
        // Convert IPv6 localhost to IPv4
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        
        return remoteAddr;
    }

    // --- Các API cho Admin sẽ nằm trong AdminOrderController hoặc AdminController ---
}