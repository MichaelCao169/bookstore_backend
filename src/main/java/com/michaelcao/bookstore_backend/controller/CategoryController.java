package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.category.CategoryDTO;
import com.michaelcao.bookstore_backend.dto.category.CreateCategoryRequest;
import com.michaelcao.bookstore_backend.dto.category.UpdateCategoryRequest;
import com.michaelcao.bookstore_backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // Base path chung cho API
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    // --- Endpoint cho Admin ---

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được phép tạo
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("Admin request received to create category: {}", request.getName());
        CategoryDTO createdCategory = categoryService.createCategory(request);
        // Trả về 201 Created cùng với thông tin category vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được phép cập nhật
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("Admin request received to update category ID: {}", id);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory); // Trả về 200 OK
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ Admin được phép xóa
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Admin request received to delete category ID: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }

    // --- Endpoint cho Public ---

    @GetMapping("/categories/{id}")
    // @PreAuthorize("permitAll()") // Không cần thiết vì mặc định SecurityConfig đã cho phép
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.debug("Public request received to get category ID: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories")
    // @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.debug("Public request received to get all categories");
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}