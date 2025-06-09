package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.review.CreateReviewRequest;
import com.michaelcao.bookstore_backend.dto.review.ReviewDTO;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Endpoint để thêm review mới cho sản phẩm.
     * Yêu cầu người dùng đã đăng nhập.
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Add review request for product ID {} by user ID {}", productId, currentUser.getId());
        ReviewDTO reviewDTO = reviewService.addReview(currentUser.getId(), productId, request);
        return ResponseEntity.ok(reviewDTO);
    }

    /**
     * Endpoint để lấy tất cả reviews của một sản phẩm.
     * Không yêu cầu đăng nhập.
     */
    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("Get reviews for product ID {}, page {}, size {}", 
                productId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ReviewDTO> reviewPage = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(reviewPage);
    }

    /**
     * Endpoint để cập nhật review.
     * Yêu cầu người dùng đã đăng nhập và chỉ có thể cập nhật review của chính mình.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable UUID productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Update review request for review ID {} by user ID {}", reviewId, currentUser.getId());
        ReviewDTO reviewDTO = reviewService.updateReview(reviewId, currentUser.getId(), request);
        return ResponseEntity.ok(reviewDTO);
    }

    /**
     * Endpoint để xóa review.
     * Yêu cầu người dùng đã đăng nhập và chỉ có thể xóa review của chính mình.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Delete review request for review ID {} by user ID {}", reviewId, currentUser.getId());
        reviewService.deleteReview(reviewId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
} 