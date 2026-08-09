package com.storix.api.domain.chat.controller;

import com.storix.api.domain.chat.usecase.ChatUseCase;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @MessageMapping("/chat/message")
    public void message(@Valid @Payload ChatMessageRequestDto request, SimpMessageHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();

        if (auth == null) {
            log.warn(">>>> [채팅] 인증되지 않은 세션입니다.");
            return;
        }

        AuthUserDetails user = (AuthUserDetails) auth.getPrincipal();
        chatUseCase.sendMessage(user.getUserId(), request);
    }

    // STOMP 는 GlobalExceptionHandler 를 타지 않아 여기서 남기지 않으면 흔적 없이 버려진다
    @MessageExceptionHandler
    public void handleMessageException(Exception e, SimpMessageHeaderAccessor accessor) {
        log.error(">>>> [채팅] 메시지 처리 실패 sessionId={}, destination={}, exceptionType={}, message={}",
                accessor.getSessionId(),
                accessor.getDestination(),
                e.getClass().getSimpleName(),
                e.getMessage());
    }
}