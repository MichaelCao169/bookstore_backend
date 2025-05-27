package com.michaelcao.bookstore_backend.config;

import com.michaelcao.bookstore_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract JWT token from Authorization header
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            log.debug("WebSocket CONNECT - Authorization header: {}", bearerToken);
            
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                log.debug("WebSocket CONNECT - Extracted token: {}", token.substring(0, Math.min(20, token.length())) + "...");
                
                try {
                    // Extract username from JWT
                    String username = jwtUtil.extractUsername(token);
                    log.debug("WebSocket CONNECT - Extracted username: {}", username);
                    
                    if (username != null) {
                        // Load user details
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Validate token
                        if (jwtUtil.isTokenValid(token, userDetails)) {
                            // Create authentication token
                            Authentication authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            
                            // Set authentication in accessor
                            accessor.setUser(authToken);
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            
                            log.info("WebSocket authentication successful for user: {}", username);
                        } else {
                            log.warn("WebSocket authentication failed - invalid token for user: {}", username);
                            throw new IllegalArgumentException("Invalid JWT token");
                        }
                    } else {
                        log.warn("WebSocket authentication failed - could not extract username from token");
                        throw new IllegalArgumentException("Could not extract username from token");
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication error: {}", e.getMessage());
                    throw new IllegalArgumentException("WebSocket authentication failed", e);
                }
            } else {
                log.warn("WebSocket CONNECT - No valid Authorization header found");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
        }
        
        return message;
    }
}
