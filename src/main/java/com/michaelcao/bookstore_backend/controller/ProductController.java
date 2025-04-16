package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.product.CreateProductRequest;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.dto.product.UpdateProductRequest;
import com.michaelcao.bookstore_backend.dto.review.CreateReviewRequest;
import com.michaelcao.bookstore_backend.dto.review.ReviewDTO;
import com.michaelcao.bookstore_backend.entity.User; // Import User
import com.michaelcao.bookstore_backend.service.ProductService;
import com.michaelcao.bookstore_backend.service.ReviewService; // *** ĐẢM BẢO ĐÃ IMPORT ***
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor // Lombok sẽ tự tạo constructor cho các field final
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService; // *** THÊM DEPENDENCY NÀY VÀO KHAI BÁO FINAL ***

    // *** THÊM LẠI PHƯƠNG THỨC HELPER NÀY ***
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            return currentUser.getId();
        }
        log.warn("Could not get current user ID from Security Context.");
        // Trả về null hoặc ném lỗi tùy logic xử lý mong muốn ở nơi gọi
        return null;
        // Hoặc: throw new IllegalStateException("User not authenticated properly.");
    }

    // --- Admin Endpoints ---
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin request received to create product: {}", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        log.info("Admin request received to update product ID: {}", id);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Admin request received to delete product ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --- Public Endpoints ---

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.debug("Public request received to get product ID: {}", id);
        // Sửa lại: Chỉ gọi 1 lần
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) String author, // Thêm tham số lọc author
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        log.debug("Request received to filter products with criteria - CategoryId: {}, Keyword: '{}', MinPrice: {}, MaxPrice: {}, InStockOnly: {}, Author: '{}', Pageable: {}",
                categoryId, keyword, minPrice, maxPrice, inStockOnly, author, pageable);

        // Gọi phương thức service đã cập nhật
        Page<ProductDTO> productPage = productService.filterProducts(
                categoryId, keyword, minPrice, maxPrice, inStockOnly, author, pageable
        );
        return ResponseEntity.ok(productPage);
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        log.debug("Public request received to get products for category ID: {} with pagination: {}", categoryId, pageable);
        Page<ProductDTO> productPage = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        log.debug("Public request received to search products with keyword: '{}', pagination: {}", keyword, pageable);
        Page<ProductDTO> productPage = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(productPage);
    }

    // --- Review Endpoints ---

    @PostMapping("/{productId}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewDTO> addProductReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request) {

        Long userId = getCurrentUserId(); // Bây giờ phương thức này tồn tại
        if (userId == null) {
            log.error("Unauthorized attempt to add review for product ID: {}", productId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Request received to add review for product ID {} by user ID {}", productId, userId);
        // Gọi phương thức từ reviewService đã được inject
        ReviewDTO createdReview = reviewService.addReview(userId, productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
            @PathVariable Long productId,
            // Sửa lại direction cho đúng kiểu Enum
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Request received to get reviews for product ID {} with pagination: {}", productId, pageable);
        // Gọi phương thức từ reviewService đã được inject
        Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(reviewPage);
    }

    // --- Optional Endpoints (Giữ nguyên comment) ---
}