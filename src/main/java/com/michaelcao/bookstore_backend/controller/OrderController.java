package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.order.CreateOrderRequest;
import com.michaelcao.bookstore_backend.dto.order.OrderDTO;
import com.michaelcao.bookstore_backend.entity.User; // Import User
import com.michaelcao.bookstore_backend.service.OrderService;
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

@RestController
@RequestMapping("/api/orders") // Base path cho các API liên quan đến đơn hàng của người dùng
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')") // Yêu cầu quyền CUSTOMER cho tất cả API trong controller này
public class OrderController {

    private final OrderService orderService;

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
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = getCurrentUserId();
        log.info("Request received to create order for user ID: {} with payment method: {}", userId, request.getPaymentMethod());
        // Service sẽ xử lý việc lấy giỏ hàng, kiểm tra, tạo đơn hàng, trừ kho, xóa giỏ (nếu COD)
        OrderDTO createdOrder = orderService.createOrder(userId, request);
        // Trả về 201 Created nếu thành công (hoặc 200 OK cũng chấp nhận được)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        // TODO: Nếu là thanh toán online (VNPAY), response thực tế sẽ chứa paymentUrl thay vì OrderDTO trực tiếp.
        // Cần sửa lại logic trả về trong OrderService và Controller khi implement VNPAY.
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
    public ResponseEntity<OrderDTO> getMyOrderDetails(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        log.info("Request received to get details for order ID: {} for user ID: {}", orderId, userId);
        OrderDTO orderDetails = orderService.getOrderDetails(userId, orderId);
        return ResponseEntity.ok(orderDetails);
    }

    // --- Các API cho Admin sẽ nằm trong AdminOrderController hoặc AdminController ---
}