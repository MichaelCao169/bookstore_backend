package com.michaelcao.bookstore_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Endpoint để upload avatar của người dùng.
     * Cho phép các định dạng hình ảnh phổ biến: JPEG, PNG, GIF.
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Kiểm tra file rỗng
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn file để tải lên"));
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận file hình ảnh"));
            }

            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDirectory, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file ngẫu nhiên để tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // Tạo URL để truy cập file
            String fileUrl = appUrl + "/api/uploads/avatars/" + newFilename;

            // Trả về URL của file đã upload
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            log.info("Avatar uploaded successfully: {}", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload avatar", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể tải lên avatar: " + e.getMessage()));
        }
    }

    /**
     * Endpoint để truy cập các file avatar đã upload.
     */
    @GetMapping("/avatars/{filename:.+}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory, "avatars", filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            
            return ResponseEntity.ok()
                    .header("Content-Type", mimeType != null ? mimeType : "application/octet-stream")
                    .body(fileContent);
        } catch (IOException e) {
            log.error("Failed to retrieve avatar", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 