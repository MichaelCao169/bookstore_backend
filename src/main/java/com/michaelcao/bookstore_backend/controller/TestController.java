package com.michaelcao.bookstore_backend.controller; // Ensure correct package

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal; // To get authenticated user info
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private Environment environment;
    
    // Check if we're in development environment
    private boolean isDevelopment() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev") || 
               Arrays.asList(activeProfiles).contains("development") ||
               "true".equals(environment.getProperty("app.development-mode"));
    }

    @GetMapping("/hello-public")
    public ResponseEntity<String> helloPublic() {
        return ResponseEntity.ok("Hello, server is running");
    }

    // Requires any authenticated user
    @GetMapping("/hello-secure")
    public ResponseEntity<String> helloSecure(Principal principal) {
        // Principal will be non-null if authentication was successful
        String username = (principal != null) ? principal.getName() : "UNKNOWN";
        return ResponseEntity.ok("Hello from Secure Endpoint! User: " + username);
    }

    // Requires ADMIN role
    @GetMapping("/hello-admin")
    @PreAuthorize("hasRole('ADMIN')") // Method-level security check
    public ResponseEntity<String> helloAdmin(Principal principal) {
        String username = (principal != null) ? principal.getName() : "UNKNOWN";
        return ResponseEntity.ok("Hello from Admin Endpoint! Admin User: " + username);
    }

    // Example endpoint accessible only by CUSTOMER
    @GetMapping("/hello-customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> helloCustomer(Principal principal) {
        String username = (principal != null) ? principal.getName() : "UNKNOWN";
        return ResponseEntity.ok("Hello from Customer Endpoint! Customer User: " + username);
    }
    
    // DEVELOPMENT ONLY - Mock dashboard stats for testing without authentication
    @GetMapping("/dev/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        // Only allow in development mode
        if (!isDevelopment()) {
            return ResponseEntity.status(403).body("This endpoint is only available in development mode");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", 156);
        stats.put("totalOrders", 74);
        stats.put("totalUsers", 42);
        stats.put("totalRevenue", 15750000);
        
        List<Map<String, Object>> recentOrders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", "mock-" + (i+1));
            order.put("orderNumber", "ORD-" + (10000 + i));
            order.put("total", 100000 + (i * 50000));
            order.put("status", List.of("PENDING", "PROCESSING", "COMPLETED", "CANCELLED").get(i % 4));
            order.put("createdAt", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_DATE_TIME));
            
            Map<String, Object> user = new HashMap<>();
            user.put("name", "Khách hàng mẫu " + (i+1));
            user.put("email", "customer" + (i+1) + "@example.com");
            order.put("user", user);
            
            recentOrders.add(order);
        }
        stats.put("recentOrders", recentOrders);
        
        return ResponseEntity.ok(stats);
    }
    
    // DEVELOPMENT ONLY - Mock products list for testing without authentication
    @GetMapping("/dev/products")
    public ResponseEntity<?> getProducts() {
        // Only allow in development mode
        if (!isDevelopment()) {
            return ResponseEntity.status(403).body("This endpoint is only available in development mode");
        }
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", "mock-product-" + (i+1));
            product.put("title", "Sách mẫu " + (i+1));
            product.put("price", 100000 + (i * 20000));
            product.put("coverLink", "/product-placeholder.jpg");
            product.put("stock", 10 + i);
            product.put("createdAt", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_DATE_TIME));
            products.add(product);
        }
        
        response.put("content", products);
        response.put("totalPages", 1);
        response.put("totalElements", products.size());
        response.put("size", 10);
        response.put("number", 0);
        
        return ResponseEntity.ok(response);
    }

    // DEVELOPMENT ONLY - Test soldCount functionality
    @GetMapping("/dev/test-sold-count")
    public ResponseEntity<?> testSoldCount() {
        // Only allow in development mode
        if (!isDevelopment()) {
            return ResponseEntity.status(403).body("This endpoint is only available in development mode");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "SoldCount functionality test");
        response.put("instructions", Arrays.asList(
            "1. Create some products via admin panel",
            "2. Create some orders with those products",
            "3. Change order status to DELIVERED via admin panel",
            "4. Check if soldCount is updated automatically",
            "5. Use /api/admin/dashboard/recalculate-sold-count to recalculate all soldCount values",
            "6. Check /api/products/top-selling to see top selling products",
            "7. Check admin dashboard to see top selling products displayed"
        ));
        response.put("endpoints", Arrays.asList(
            "GET /api/products/top-selling - Get top selling products",
            "GET /api/admin/dashboard/top-products - Get top selling products (admin)",
            "POST /api/admin/dashboard/recalculate-sold-count - Recalculate soldCount for all products"
        ));
        
        return ResponseEntity.ok(response);
    }
}