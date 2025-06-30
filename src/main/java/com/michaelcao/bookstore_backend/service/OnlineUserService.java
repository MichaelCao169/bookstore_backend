package com.michaelcao.bookstore_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Dịch vụ để theo dõi người dùng đang online thông qua kết nối WebSocket
 */
@Service
@Slf4j
public class OnlineUserService {
    
    // Theo dõi người dùng đang online bằng ID của họ
    private final ConcurrentHashMap<Long, String> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * Đánh dấu một người dùng là online
     * @param userId The user ID
     * @param sessionId The WebSocket session ID
     */
    public void addOnlineUser(Long userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        log.debug("User {} is now online (session: {})", userId, sessionId);
    }
    
    /**
     * Đánh dấu một người dùng là offline
     * @param userId The user ID
     */
    public void removeOnlineUser(Long userId) {
        String sessionId = onlineUsers.remove(userId);
        if (sessionId != null) {
            log.debug("User {} is now offline (session: {})", userId, sessionId);
        }
    }
    
    /**
     * Xóa người dùng bằng ID phiên (khi phiên kết nối bị ngắt)
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
     * Kiểm tra xem một người dùng có online không
     * @param userId The user ID
     * @return true if user is online, false otherwise
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
    
    /**
     * Lấy tất cả ID của người dùng online
     * @return Set of online user IDs
     */
    public Set<Long> getOnlineUserIds() {
        return onlineUsers.keySet();
    }
    
    /**
     * Lấy số lượng người dùng online
     * @return Number of online users
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    /**
     * Kiểm tra xem có admin nào online không
     */
    public boolean isAnyAdminOnline() {
        // Hiện tại, chúng ta sẽ giả sử nếu có người dùng online, một admin có thể online
        // Trong một implementation thực tế, bạn sẽ theo dõi người dùng admin cụ thể
        return !onlineUsers.isEmpty();
    }
}
