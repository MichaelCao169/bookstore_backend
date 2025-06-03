package com.michaelcao.bookstore_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "is_read_by_customer", nullable = false)
    private Boolean isReadByCustomer = false;

    @Column(name = "is_read_by_admin", nullable = false)
    private Boolean isReadByAdmin = false;

    // File-related fields for FILE type messages
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT, IMAGE, FILE
    }

    // Helper methods
    public void markAsReadByCustomer() {
        this.isReadByCustomer = true;
    }

    public void markAsReadByAdmin() {
        this.isReadByAdmin = true;
    }    public boolean isFromAdmin() {
        return sender.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    public boolean isFromCustomer() {
        return sender.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_CUSTOMER"));
    }
}
