package com.michaelcao.bookstore_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String messageType;
    private Boolean isReadByCustomer;
    private Boolean isReadByAdmin;
    private LocalDateTime createdAt;
    private Boolean isFromAdmin;
}
