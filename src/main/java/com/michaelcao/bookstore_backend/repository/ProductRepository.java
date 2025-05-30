package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Product;
import org.springframework.data.domain.Page; // Import Page for pagination
import org.springframework.data.domain.Pageable; // Import Pageable for pagination info
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query for custom JPQL/SQL
import org.springframework.data.repository.query.Param; // Import Param for named parameters
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Find all products ordered by sold count in descending order
     * @return List of products sorted by number sold
     */
    List<Product> findAllByOrderBySoldCountDesc();

    // Tìm sản phẩm theo ISBN (duy nhất)
    Optional<Product> findByIsbn(String isbn);

    // Tìm tất cả sản phẩm thuộc về một Category cụ thể (có phân trang)
    // Sử dụng ID của Category để tìm kiếm
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Tìm tất cả sản phẩm thuộc về một Category cụ thể (dùng đối tượng Category)
    // Page<Product> findByCategory(Category category, Pageable pageable); // Cách khác

    // Tìm sản phẩm có tiêu đề chứa một chuỗi (không phân biệt chữ hoa/thường, có phân trang)
    Page<Product> findByTitleContainingIgnoreCase(String titleKeyword, Pageable pageable);

    // Tìm sản phẩm có tác giả chứa một chuỗi (không phân biệt chữ hoa/thường, có phân trang)
    Page<Product> findByAuthorContainingIgnoreCase(String authorKeyword, Pageable pageable);

    // Ví dụ về truy vấn phức tạp hơn sử dụng JPQL (Java Persistence Query Language)
    // Tìm sản phẩm theo keyword trong title hoặc author (có phân trang)
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(p.author) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Product> searchByTitleOrAuthor(@Param("keyword") String keyword, Pageable pageable);

    // Kiểm tra sự tồn tại của sản phẩm theo ISBN
    boolean existsByIsbn(String isbn);

    // Đếm số lượng sản phẩm theo category ID (dùng để kiểm tra trước khi xóa Category)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    // Tìm sản phẩm theo nhiều danh mục
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id IN :categoryIds")
    Page<Product> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // Tìm sản phẩm theo từ khóa trong tiêu đề hoặc tác giả
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Bạn có thể thêm nhiều phương thức truy vấn khác dựa trên nhu cầu
}