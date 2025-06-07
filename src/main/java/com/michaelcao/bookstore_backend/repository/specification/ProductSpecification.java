package com.michaelcao.bookstore_backend.repository.specification;

import com.michaelcao.bookstore_backend.entity.Category; // Import Category nếu lọc theo Category object
import com.michaelcao.bookstore_backend.entity.Product;
import jakarta.persistence.criteria.*; // Import các thành phần của Criteria API
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.util.StringUtils; // Import StringUtils

import java.math.BigDecimal;
import java.util.ArrayList; // Import ArrayList
import java.util.List;    // Import List

public class ProductSpecification {

    /**
     * Tạo Specification để tìm theo keyword trong title hoặc author (không phân biệt hoa thường).
     */
    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return criteriaBuilder.conjunction(); // Trả về điều kiện luôn đúng nếu keyword rỗng
            }
            String keywordLower = "%" + keyword.toLowerCase().trim() + "%";
            // Tạo Predicate cho title LIKE keyword OR author LIKE keyword
            Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordLower);
            Predicate authorLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), keywordLower);
            return criteriaBuilder.or(titleLike, authorLike); // Kết hợp bằng OR
        };
    }

    /**
     * Tạo Specification để lọc theo Category ID.
     */
    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null || categoryId <= 0) {
                return criteriaBuilder.conjunction(); // Bỏ qua nếu categoryId không hợp lệ
            }
            // Tạo Predicate: product.category.id = categoryId
            // Cần join đến Category để lấy id
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER); // INNER JOIN vì product phải có category
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);

            // Hoặc cách khác nếu không muốn join tường minh:
            // return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
            // Hibernate thường tự hiểu cần join
        };
    }

    /**
     * Tạo Specification để lọc theo khoảng giá.
     */
    public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) > 0) {
                // Thêm điều kiện currentPrice >= minPrice
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("currentPrice"), minPrice));
            }
            if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0 && (minPrice == null || maxPrice.compareTo(minPrice) >= 0)) {
                // Thêm điều kiện currentPrice <= maxPrice (chỉ thêm nếu max hợp lệ và >= min)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("currentPrice"), maxPrice));
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction(); // Không có điều kiện giá
            }
            // Kết hợp các điều kiện giá bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tạo Specification để lọc sản phẩm còn hàng.
     */
    public static Specification<Product> isAvailable() {
        return (root, query, criteriaBuilder) ->
                // Điều kiện: quantity > 0
                criteriaBuilder.greaterThan(root.get("quantity"), 0);
    }

    /**
     * (Ví dụ) Tạo Specification để lọc theo tác giả chính xác (không phân biệt hoa thường).
     */
    public static Specification<Product> hasAuthor(String author) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(author)) {
                return criteriaBuilder.conjunction();
            }
            // Điều kiện: LOWER(author) = LOWER(?)
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("author")), author.toLowerCase().trim());
        };
    }

    // --- Phương thức kết hợp các Specification ---
    public static Specification<Product> buildSpecification(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly, String author) {
        Specification<Product> spec = Specification.where(null); // Bắt đầu với spec luôn đúng

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(hasKeyword(keyword));
        }
        if (categoryId != null && categoryId > 0) {
            spec = spec.and(hasCategoryId(categoryId));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(priceBetween(minPrice, maxPrice));
        }
        if (inStockOnly != null && inStockOnly) { // Nếu client gửi inStockOnly=true
            spec = spec.and(isAvailable());
        }
        if (StringUtils.hasText(author)) {
            spec = spec.and(hasAuthor(author));
        }

        return spec;
    }
}