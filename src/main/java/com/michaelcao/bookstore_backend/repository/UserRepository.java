package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.User;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Tìm kiếm user theo tên hoặc email (không phân biệt hoa thường, có phân trang).
     * Cần JOIN FETCH roles nếu muốn tối ưu N+1 khi lấy danh sách.
     * @param nameKeyword Từ khóa cho tên.
     * @param emailKeyword Từ khóa cho email.
     * @param pageable Thông tin phân trang.
     * @return Page chứa danh sách User.
     */
    // Tạm thời chưa JOIN FETCH roles
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword, Pageable pageable);

    // Ví dụ query JOIN FETCH roles (phức tạp hơn cho Page):
    // @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.name) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(concat('%', :keyword, '%'))",
    //        countQuery = "SELECT count(u) FROM User u WHERE LOWER(u.name) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(concat('%', :keyword, '%'))")
    // Page<User> searchByNameOrEmailWithRoles(@Param("keyword") String keyword, Pageable pageable);

}