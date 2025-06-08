package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.aichat.AiChatRequest;
import com.michaelcao.bookstore_backend.dto.aichat.AiChatResponse;
import com.michaelcao.bookstore_backend.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
@Slf4j
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/send")
    public ResponseEntity<AiChatResponse> getAiResponse(@Valid @RequestBody AiChatRequest request) {
        log.info("AI Chat: Received chat request");
        
        try {
            String reply = aiChatService.getAiResponse(request.getMessage());
            log.info("AI Chat: Successfully processed request");
            return ResponseEntity.ok(new AiChatResponse(reply));
        } catch (Exception e) {
            log.error("AI Chat: Error processing request", e);
            return ResponseEntity.ok(new AiChatResponse("Xin lỗi, tôi đang gặp một sự cố kỹ thuật. Vui lòng thử lại sau."));
        }
    }
}