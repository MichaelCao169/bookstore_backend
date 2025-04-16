package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Hashed password

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = false; // Default: needs verification

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    // *** THÊM QUAN HỆ WISHLIST ***
    // Quan hệ Nhiều-Nhiều: Một User có thể thích nhiều Product, một Product có thể được nhiều User thích.
    @ManyToMany(fetch = FetchType.LAZY) // LAZY để không tải wishlist mỗi khi load User
    @JoinTable(
            name = "user_wishlist", // Tên bảng join
            joinColumns = @JoinColumn(name = "user_id"), // Khóa ngoại trỏ về bảng users
            inverseJoinColumns = @JoinColumn(name = "product_id") // Khóa ngoại trỏ về bảng products
    )
    @Builder.Default // Cho Lombok builder
    private Set<Product> wishlistItems = new HashSet<>();
    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }

    public void addRole(Role role) { this.roles.add(role); }

    // Helper methods (tùy chọn)
    public void addToWishlist(Product product) {
        this.wishlistItems.add(product);
        // Không cần set ngược lại ở Product vì đây là quan hệ ManyToMany do User quản lý join table
    }

    public void removeFromWishlist(Product product) {
        this.wishlistItems.remove(product);
    }

}