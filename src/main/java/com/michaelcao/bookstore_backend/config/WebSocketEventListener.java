package com.michaelcao.bookstore_backend.config;

import com.michaelcao.bookstore_backend.service.OnlineUserService;
import com.michaelcao.bookstore_backend.entity.User;
import com.michaelcao.bookstore_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {    private final OnlineUserService onlineUserService;
    private final UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("WebSocket connection established - Session ID: {}", sessionId);
          // Extract user information from the session
        if (headerAccessor.getUser() != null) {
            try {
                // Get the user ID from the authenticated user
                String username = headerAccessor.getUser().getName();
                
                // Look up user by email/username to get the actual User ID
                User user = userRepository.findByEmail(username).orElse(null);
                
                if (user != null) {
                    onlineUserService.addOnlineUser(user.getId(), sessionId);
                    log.info("User {} (ID: {}) connected via WebSocket (session: {})", username, user.getId(), sessionId);
                } else {
                    log.warn("Could not find user with email/username: {}", username);
                }
            } catch (Exception e) {
                log.error("Error processing WebSocket connection for session {}: {}", sessionId, e.getMessage());
            }
        } else {
            log.warn("WebSocket connection without authenticated user - Session ID: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.debug("WebSocket connection closed - Session ID: {}", sessionId);
          // Remove user from online users by session ID
        onlineUserService.removeUserBySession(sessionId);
        log.info("User disconnected from WebSocket (session: {})", sessionId);
    }
}
