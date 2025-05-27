package com.michaelcao.bookstore_backend.config; // Ensure correct package

import com.michaelcao.bookstore_backend.security.jwt.JwtAuthenticationFilter; // Ensure correct import
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Import CORS configurer if needed
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;    // Public endpoints that don't require authentication
    private static final String[] PUBLIC_MATCHERS = {
            "/api/auth/**",
            "/api/verify-email/**",
            "/api/reset-password/**",
            "/api/test/hello-public",
            "/api/uploads/**",
            "/ws/**", // WebSocket endpoint
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API 
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> {})
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                
                // Public GET endpoints for products and categories
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Customer-specific endpoints
                .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
                .requestMatchers("/api/wishlist/**").hasRole("CUSTOMER")
                  // Admin-specific endpoints
                .requestMatchers("/api/products/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Chat endpoints - both admin and customer can access
                .requestMatchers("/api/chat/**").authenticated()
                
                // User profile endpoints require any authenticated user
                .requestMatchers("/api/profile/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Set stateless session management for REST API
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider)
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
