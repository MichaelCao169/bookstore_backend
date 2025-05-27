package com.michaelcao.bookstore_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerAvatar;
    private Long adminId;
    private String adminName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTimestamp;
    private Integer unreadCountCustomer;
    private Integer unreadCountAdmin;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCustomerOnline;
    private List<MessageResponse> recentMessages;
}
