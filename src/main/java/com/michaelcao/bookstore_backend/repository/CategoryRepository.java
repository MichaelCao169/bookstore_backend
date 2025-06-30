package com.michaelcao.bookstore_backend.repository;

import com.michaelcao.bookstore_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; 

@Repository 
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Spring Data JPA tự động tạo query dựa trên tên phương thức:
    // Tìm Category bằng tên (không phân biệt chữ hoa/thường)
    Optional<Category> findByNameIgnoreCase(String name);

    // Kiểm tra xem Category với tên cho trước có tồn tại không (không phân biệt chữ hoa/thường)
    boolean existsByNameIgnoreCase(String name);

}