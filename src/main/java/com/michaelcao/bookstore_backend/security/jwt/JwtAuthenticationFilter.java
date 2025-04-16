package com.michaelcao.bookstore_backend.security.jwt; // Ensure correct package

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check for Bearer token
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Proceed without setting auth if no token
            return;
        }

        // 2. Extract token
        jwt = authHeader.substring(7);

        // 3. Extract username (email)
        try { // Add try-catch around extraction as it might fail
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("Could not extract username from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response); // Proceed, let other filters handle potential 401/403
            return;
        }


        // 4. Check if username exists and no authentication is already in context
        if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Validate token against user details
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                // 6. Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No credentials needed for JWT auth
                        userDetails.getAuthorities()
                );
                // 7. Set details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 8. Update SecurityContextHolder *** IMPORTANT ***
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Successfully authenticated user '{}' via JWT.", userEmail);
            } else {
                log.warn("JWT token validation failed for user '{}'. Token might be invalid or expired.", userEmail);
                // Do not set authentication if token is invalid
            }
        } else {
            if (!StringUtils.hasText(userEmail)) {
                log.debug("Could not extract username from JWT (already logged)."); // Should have been caught earlier, but good failsafe log
            }
            // Optional: Log if auth already exists (can happen with other auth mechanisms or repeated filter calls)
            // if (SecurityContextHolder.getContext().getAuthentication() != null) {
            //     log.trace("SecurityContext already holds Authentication for user '{}'.", SecurityContextHolder.getContext().getAuthentication().getName());
            // }
        }

        // 9. Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}