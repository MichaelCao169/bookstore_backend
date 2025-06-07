package com.michaelcao.bookstore_backend.service.impl;

import com.michaelcao.bookstore_backend.dto.product.ProductSummaryDTO; // Import DTO tóm tắt
import com.michaelcao.bookstore_backend.dto.wishlist.WishlistDTO;
import com.michaelcao.bookstore_backend.entity.Product;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.exception.DuplicateResourceException;
import com.michaelcao.bookstore_backend.exception.ResourceNotFoundException;
import com.michaelcao.bookstore_backend.repository.ProductRepository;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import com.michaelcao.bookstore_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Cần Transactional

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Helper method: Map Product sang ProductSummaryDTO
    private ProductSummaryDTO mapToProductSummaryDTO(Product product) {
        return new ProductSummaryDTO(
                product.getProductId(),
                product.getTitle(),
                product.getAuthor(),
                product.getCurrentPrice(),
                product.getCoverLink(),
                (product.getCategory() != null) ? product.getCategory().getName() : null // Lấy tên Category nếu có
        );
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc
    public WishlistDTO getWishlist(Long userId) {
        log.debug("Fetching wishlist for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        // Truy cập user.getWishlistItems() sẽ trigger LAZY loading
        // Map các Product trong wishlist sang DTO tóm tắt
        List<ProductSummaryDTO> items = user.getWishlistItems().stream()
                .map(this::mapToProductSummaryDTO)
                .collect(Collectors.toList());

        return new WishlistDTO(items);
    }

    @Override
    @Transactional // Cần Transaction để cập nhật User
    public void addProductToWishlist(Long userId, UUID productId) {
        log.info("Attempting to add product ID {} to wishlist for user ID {}", productId, userId);
        // Lấy User (cần fetch EAGER hoặc trong transaction để sửa collection)
        // findById sẽ trả về managed entity trong transaction này
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));

        // Kiểm tra xem sản phẩm đã có trong wishlist chưa
        // if (user.getWishlistItems().contains(product)) { // Cách này có thể gây load cả collection
        // Tối ưu hơn: Kiểm tra qua query Repository nếu collection lớn
        // Hoặc kiểm tra trực tiếp trên Set nếu đã load (cẩn thận lazy loading)
        boolean alreadyExists = user.getWishlistItems().stream().anyMatch(p -> p.getProductId().equals(productId));
        if (alreadyExists) {
            log.warn("Product ID {} already exists in wishlist for user ID {}", productId, userId);
            throw new DuplicateResourceException("Product is already in your wishlist.");
        }

        // Thêm sản phẩm vào wishlist của user
        user.addToWishlist(product); // Dùng helper method hoặc user.getWishlistItems().add(product);

        // Lưu lại User, JPA sẽ tự động cập nhật bảng join user_wishlist
        userRepository.save(user);
        log.info("Product ID {} added to wishlist for user ID {}", productId, userId);
    }

    @Override
    @Transactional // Cần Transaction để cập nhật User
    public void removeProductFromWishlist(Long userId, UUID productId) {
        log.info("Attempting to remove product ID {} from wishlist for user ID {}", productId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        // Tìm sản phẩm cần xóa trong wishlist của user
        Product productToRemove = user.getWishlistItems().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Product ID {} not found in wishlist for user ID {}", productId, userId);
                    return new ResourceNotFoundException("Product not found in wishlist");
                });

        // Xóa sản phẩm khỏi wishlist
        user.removeFromWishlist(productToRemove); // Dùng helper method hoặc user.getWishlistItems().remove(productToRemove);

        // Lưu lại User, JPA sẽ xóa bản ghi tương ứng trong bảng join
        userRepository.save(user);
        log.info("Product ID {} removed from wishlist for user ID {}", productId, userId);
    }
}