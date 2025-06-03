package com.michaelcao.bookstore_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private String content;
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE
    
    // File-related fields for FILE/IMAGE type messages
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
}
