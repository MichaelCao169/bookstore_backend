package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.wishlist.WishlistDTO;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist") // Base path cho wishlist
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')") // Chỉ Customer mới có wishlist và thao tác được
public class WishlistController {

    private final WishlistService wishlistService;

    // Helper method để lấy User ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            return currentUser.getId();
        }
        log.error("Could not get current user ID from Security Context for wishlist action.");
        throw new IllegalStateException("User not authenticated properly.");
    }

    /**
     * Endpoint để lấy danh sách sản phẩm yêu thích của người dùng hiện tại.
     */
    @GetMapping
    public ResponseEntity<WishlistDTO> getMyWishlist() {
        Long userId = getCurrentUserId();
        log.info("Request received to get wishlist for user ID: {}", userId);
        WishlistDTO wishlist = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Endpoint để thêm một sản phẩm vào wishlist.
     * productId được lấy từ path variable.
     */
    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> addProductToMyWishlist(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.info("Request received to add product ID {} to wishlist for user ID: {}", productId, userId);
        wishlistService.addProductToWishlist(userId, productId);
        // Trả về 200 OK hoặc 204 No Content đều được
        return ResponseEntity.ok().build();
        // Hoặc return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint để xóa một sản phẩm khỏi wishlist.
     * productId được lấy từ path variable.
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> removeProductFromMyWishlist(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.info("Request received to remove product ID {} from wishlist for user ID: {}", productId, userId);
        wishlistService.removeProductFromWishlist(userId, productId);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}