package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.dashboard.DashboardStatsDTO;
import com.michaelcao.bookstore_backend.dto.product.ProductDTO;
import com.michaelcao.bookstore_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

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
} 