package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.chat.ConversationResponse;
import com.michaelcao.bookstore_backend.dto.chat.MessageResponse;
import com.michaelcao.bookstore_backend.dto.chat.SendMessageRequest;
import com.michaelcao.bookstore_backend.security.jwt.JwtUtil;
import com.michaelcao.bookstore_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    
    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    
    // Customer endpoints
    
    @PostMapping("/customer/send")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<MessageResponse> sendMessageFromCustomer(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        
        Long customerId = getUserIdFromToken(httpRequest);
        MessageResponse response = chatService.sendMessageFromCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/conversation")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ConversationResponse> getCustomerConversation(HttpServletRequest httpRequest) {
        Long customerId = getUserIdFromToken(httpRequest);
        ConversationResponse response = chatService.getCustomerConversation(customerId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/customer/conversation/{conversationId}/messages")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        
        // Additional security check could be added here to ensure customer owns the conversation
        List<MessageResponse> messages = chatService.getConversationMessages(conversationId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/customer/conversation/{conversationId}/mark-read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> markMessagesAsReadByCustomer(
            @PathVariable Long conversationId,
            HttpServletRequest httpRequest) {
        
        Long customerId = getUserIdFromToken(httpRequest);
        chatService.markMessagesAsReadByCustomer(customerId, conversationId);
        return ResponseEntity.ok().build();
    }
    
    // Admin endpoints
    
    @GetMapping("/admin/conversations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConversationResponse>> getAdminConversations() {
        List<ConversationResponse> conversations = chatService.getAdminConversations();
        return ResponseEntity.ok(conversations);
    }
    
    @PostMapping("/admin/conversation/{conversationId}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> sendMessageFromAdmin(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        
        Long adminId = getUserIdFromToken(httpRequest);
        MessageResponse response = chatService.sendMessageFromAdmin(adminId, conversationId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/conversation/{conversationId}/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MessageResponse>> getAdminConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        List<MessageResponse> messages = chatService.getConversationMessages(conversationId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/admin/conversation/{conversationId}/mark-read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markMessagesAsReadByAdmin(@PathVariable Long conversationId) {
        chatService.markMessagesAsReadByAdmin(conversationId);
        return ResponseEntity.ok().build();
    }
      // Helper method to extract user ID from JWT token
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        if (token != null) {
            try {
                return jwtUtil.extractUserId(token);
            } catch (Exception e) {
                throw new RuntimeException("Invalid token", e);
            }
        }
        throw new RuntimeException("Missing token");
    }
    
    // Extract JWT from request's Authorization header
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
