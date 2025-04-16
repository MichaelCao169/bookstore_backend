package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.order.OrderDTO;
import com.michaelcao.bookstore_backend.dto.order.UpdateOrderStatusRequest;
import com.michaelcao.bookstore_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders") // Base path cho API quản lý đơn hàng của Admin
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // Yêu cầu quyền ADMIN cho tất cả các endpoint trong controller này
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * Endpoint cho Admin lấy danh sách tất cả đơn hàng (phân trang).
     */
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @PageableDefault(size = 15, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("Admin request received to get all orders with pagination: {}", pageable);
        Page<OrderDTO> orderPage = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orderPage);
    }

    /**
     * Endpoint cho Admin lấy chi tiết một đơn hàng bất kỳ.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        log.info("Admin request received to get details for order ID: {}", orderId);
        OrderDTO orderDetails = orderService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(orderDetails);
    }

    /**
     * Endpoint cho Admin cập nhật trạng thái của một đơn hàng.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("Admin request received to update status for order ID: {} to {}", orderId, request.getStatus());
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }
}