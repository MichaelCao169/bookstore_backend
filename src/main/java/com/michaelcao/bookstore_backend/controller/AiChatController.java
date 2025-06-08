package com.michaelcao.bookstore_backend.controller;

import com.michaelcao.bookstore_backend.dto.aichat.AiChatRequest;
import com.michaelcao.bookstore_backend.dto.aichat.AiChatResponse;
import com.michaelcao.bookstore_backend.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
@Slf4j
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/send")
    public ResponseEntity<AiChatResponse> getAiResponse(@Valid @RequestBody AiChatRequest request) {
        log.info("Received AI chat request: '{}'", request.getMessage());
        String reply = aiChatService.getAiResponse(request.getMessage());
        return ResponseEntity.ok(new AiChatResponse(reply));
    }
}