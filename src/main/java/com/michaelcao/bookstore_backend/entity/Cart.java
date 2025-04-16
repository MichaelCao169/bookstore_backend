package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ Một-Một với User: Mỗi User có một Cart
    // optional = false: Một Cart phải thuộc về một User nào đó.
    // fetch = FetchType.LAZY: Thường không cần load User ngay khi lấy Cart.
    // @MapsId: Sử dụng cùng ID với User (không bắt buộc, nhưng là một cách thiết kế phổ biến cho OneToOne)
    //          Nếu dùng @MapsId thì không cần @GeneratedValue trên id của Cart,
    //          và kiểu ID của Cart phải giống User (Long). Cần bỏ @GeneratedValue và sửa kiểu ID nếu dùng.
    // Cách đơn giản hơn là dùng JoinColumn thông thường:
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Khóa ngoại trỏ đến bảng users, đảm bảo unique
    private User user;

    // Quan hệ Một-Nhiều với CartItem: Một Cart có nhiều CartItem
    // cascade = CascadeType.ALL: Khi lưu/cập nhật/xóa Cart, các CartItem liên quan cũng bị ảnh hưởng.
    //                          Đặc biệt hữu ích: khi xóa Cart (ít khi xảy ra) thì xóa hết item,
    //                          khi thêm/xóa item vào Set này và lưu Cart, item cũng được lưu/xóa.
    // orphanRemoval = true: Khi một CartItem bị xóa khỏi Set 'cartItems' này VÀ Cart được lưu lại,
    //                       thì CartItem đó cũng sẽ bị xóa khỏi database. Rất tiện lợi!
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    // Constructor (tùy chọn)
    public Cart(User user) {
        this.user = user;
    }

    // Helper methods để quản lý quan hệ hai chiều với CartItem
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }

    // Có thể thêm các phương thức tính toán tổng tiền ở đây, nhưng thường làm ở Service
    // public BigDecimal getTotalPrice() { ... }
}