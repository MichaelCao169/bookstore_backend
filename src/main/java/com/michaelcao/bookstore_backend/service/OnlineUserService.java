package com.michaelcao.bookstore_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Service to track online users through WebSocket connections
 */
@Service
@Slf4j
public class OnlineUserService {
    
    // Track online users by their user ID
    private final ConcurrentHashMap<Long, String> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * Mark a user as online
     * @param userId The user ID
     * @param sessionId The WebSocket session ID
     */
    public void addOnlineUser(Long userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        log.debug("User {} is now online (session: {})", userId, sessionId);
    }
    
    /**
     * Mark a user as offline
     * @param userId The user ID
     */
    public void removeOnlineUser(Long userId) {
        String sessionId = onlineUsers.remove(userId);
        if (sessionId != null) {
            log.debug("User {} is now offline (session: {})", userId, sessionId);
        }
    }
    
    /**
     * Remove user by session ID (when session disconnects)
     * @param sessionId The WebSocket session ID
     */
    public void removeUserBySession(String sessionId) {
        onlineUsers.entrySet().removeIf(entry -> {
            if (sessionId.equals(entry.getValue())) {
                log.debug("User {} is now offline (session disconnected: {})", entry.getKey(), sessionId);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Check if a user is online
     * @param userId The user ID
     * @return true if user is online, false otherwise
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
    
    /**
     * Get all online user IDs
     * @return Set of online user IDs
     */
    public Set<Long> getOnlineUserIds() {
        return onlineUsers.keySet();
    }
    
    /**
     * Get count of online users
     * @return Number of online users
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    /**
     * Check if any admin is online
     * This is a simplified check - in a real application you'd want to 
     * track user roles or have a separate admin tracking
     * @return true if any user is online (simplified assumption that admins might be online)
     */
    public boolean isAnyAdminOnline() {
        // For now, we'll assume if there are any online users, an admin might be online
        // In a real implementation, you'd track admin users specifically
        return !onlineUsers.isEmpty();
    }
}
