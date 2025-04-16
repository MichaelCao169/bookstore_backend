package com.michaelcao.bookstore_backend.controller; // Ensure correct package

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal; // To get authenticated user info

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello-public")
    public ResponseEntity<String> helloPublic() {
        return ResponseEntity.ok("Hello from Public Endpoint!");
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
}