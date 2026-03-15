package com.storix.api.domain.chat;

import com.storix.domain.domains.chat.application.usecase.ChatUseCase;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
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
    public void message(@Payload ChatMessageRequestDto request, SimpMessageHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();

        if (auth == null) {
            log.error(">>>> [채팅 에러] 인증되지 않은 세션입니다.");
            return;
        }

        AuthUserDetails user = (AuthUserDetails) auth.getPrincipal();
        chatUseCase.sendMessage(user.getUserId(), request);
    }
}