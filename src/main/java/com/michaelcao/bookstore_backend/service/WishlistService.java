package com.michaelcao.bookstore_backend.service;

import com.michaelcao.bookstore_backend.dto.wishlist.WishlistDTO;

import java.util.UUID;

public interface WishlistService {

    /**
     * Lấy danh sách sản phẩm yêu thích của người dùng.
     * @param userId ID của người dùng.
     * @return WishlistDTO chứa danh sách sản phẩm.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu không tìm thấy user.
     */
    WishlistDTO getWishlist(Long userId);

    /**
     * Thêm một sản phẩm vào danh sách yêu thích của người dùng.
     * @param userId ID của người dùng.
     * @param productId ID của sản phẩm cần thêm.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu user hoặc product không tồn tại.
     * @throws com.michaelcao.bookstore_backend.exception.DuplicateResourceException Nếu sản phẩm đã có trong wishlist.
     */
    void addProductToWishlist(Long userId, UUID productId);

    /**
     * Xóa một sản phẩm khỏi danh sách yêu thích của người dùng.
     * @param userId ID của người dùng.
     * @param productId ID của sản phẩm cần xóa.
     * @throws com.michaelcao.bookstore_backend.exception.ResourceNotFoundException Nếu user hoặc product không tồn tại, hoặc sản phẩm không có trong wishlist.
     */
    void removeProductFromWishlist(Long userId, UUID productId);
}