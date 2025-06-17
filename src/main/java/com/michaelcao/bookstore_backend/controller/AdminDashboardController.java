package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.dashboard.DashboardStatsDTO;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.service.DashboardService;
import com.michaelcao.bookstore_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final ProductService productService;

    /**
     * Get dashboard statistics overview
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        log.info("Admin request received to get dashboard statistics");
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top selling products
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<ProductDTO>> getTopSellingProducts() {
        log.info("Admin request received to get top selling products");
        List<ProductDTO> topProducts = dashboardService.getTopSellingProducts(5); // limit to top 5
        return ResponseEntity.ok(topProducts);
    }

    /**
     * Recalculate soldCount for all products based on delivered orders
     * This endpoint should be used to initialize or fix soldCount data
     */
    @PostMapping("/recalculate-sold-count")
    public ResponseEntity<Map<String, String>> recalculateSoldCount() {
        log.info("Admin request received to recalculate soldCount for all products");
        try {
            productService.recalculateSoldCountForAllProducts();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully recalculated soldCount for all products");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error recalculating soldCount", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to recalculate soldCount: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }
} 