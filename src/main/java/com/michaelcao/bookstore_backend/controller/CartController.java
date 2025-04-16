package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.cart.AddToCartRequest;
import com.michaelcao.bookstore_backend.dto.cart.CartDTO;
import com.michaelcao.bookstore_backend.dto.cart.UpdateCartItemRequest;
import com.michaelcao.bookstore_backend.entity.User; // Import User để lấy từ Principal
import com.michaelcao.bookstore_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.web.bind.annotation.*; // Import các annotations cần thiết

@RestController
@RequestMapping("/api/cart") // Base path cho tất cả API giỏ hàng
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')") // Yêu cầu quyền CUSTOMER cho tất cả API trong controller này
// Hoặc có thể dùng @PreAuthorize("isAuthenticated()") nếu cả Admin cũng có thể có giỏ hàng (ít phổ biến)
public class CartController {

    private final CartService cartService;

    // Helper method để lấy User ID từ Security Context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            // Trường hợp này ít khi xảy ra nếu @PreAuthorize hoạt động đúng,
            // nhưng là một cách kiểm tra an toàn.
            log.error("Could not retrieve authenticated user information.");
            throw new IllegalStateException("User not authenticated properly");
        }
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId();
    }

    /**
     * Endpoint để lấy giỏ hàng của người dùng hiện tại.
     */
    @GetMapping
    public ResponseEntity<CartDTO> getUserCart() {
        Long userId = getCurrentUserId();
        log.info("Request received to get cart for user ID: {}", userId);
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Endpoint để thêm sản phẩm vào giỏ hàng.
     */
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        log.info("Request received to add product ID {} quantity {} for user ID: {}",
                request.getProductId(), request.getQuantity(), userId);
        CartDTO updatedCart = cartService.addProductToCart(userId, request);
        return ResponseEntity.ok(updatedCart); // Trả về giỏ hàng đã cập nhật
    }

    /**
     * Endpoint để cập nhật số lượng của một item trong giỏ hàng.
     * cartItemId được lấy từ đường dẫn URL.
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = getCurrentUserId();
        log.info("Request received to update cart item ID {} quantity {} for user ID: {}",
                cartItemId, request.getQuantity(), userId);
        CartDTO updatedCart = cartService.updateCartItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Endpoint để xóa một item khỏi giỏ hàng.
     * cartItemId được lấy từ đường dẫn URL.
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> removeCartItem(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        log.info("Request received to remove cart item ID {} for user ID: {}", cartItemId, userId);
        CartDTO updatedCart = cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.ok(updatedCart); // Trả về giỏ hàng sau khi xóa (có thể không đổi nếu item không tồn tại)
    }

    /**
     * Endpoint để xóa toàn bộ giỏ hàng của người dùng hiện tại.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearUserCart() {
        Long userId = getCurrentUserId();
        log.info("Request received to clear cart for user ID: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}