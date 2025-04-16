package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;





@Entity
@Table(name = "orders") // Tên bảng là 'orders' (số nhiều)
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ Nhiều-Một với User: Nhiều Order có thể thuộc về một User
    @NotNull // Validation
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp // Tự động lấy thời gian tạo
    @Column(name = "order_date", nullable = false, updatable = false)
    private Instant orderDate;

    @NotNull(message = "Total amount cannot be null")
    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2) // Tổng tiền cuối cùng của đơn hàng
    private BigDecimal totalAmount;

    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING) // Lưu tên của Enum (PENDING, PROCESSING,...) vào DB thay vì số thứ tự
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING; // Trạng thái mặc định khi mới tạo

    @NotNull(message = "Payment method cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod; // Phương thức thanh toán

    // Nhúng địa chỉ giao hàng vào bảng orders
    @Embedded // Đánh dấu để nhúng các trường từ Address vào đây
    @NotNull // Địa chỉ không được null
    private Address shippingAddress;

    @Column(length = 500)
    private String notes; // Ghi chú của khách hàng (tùy chọn)

    // Quan hệ Một-Nhiều với OrderItem
    // cascade = CascadeType.ALL: Khi lưu Order, các OrderItem cũng được lưu.
    // orphanRemoval = true: Khi xóa OrderItem khỏi Set này và lưu Order, item sẽ bị xóa khỏi DB.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    // Constructor (tùy chọn)
    public Order(User user, BigDecimal totalAmount, OrderStatus status, PaymentMethod paymentMethod, Address shippingAddress) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
    }

    // Helper methods để quản lý quan hệ hai chiều với OrderItem
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}