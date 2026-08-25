package com.storix.api.domain.chat.helper;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.infrastructure.external.chat.RedisChatAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPublishHelper {

    private final RedisChatAdapter redisChatAdapter;

    public void publish(ChatMessage chatMessage, StandardProfileInfo sender) {
        redisChatAdapter.publish(ChatMessageResponseDto.of(
                chatMessage, sender.nickName(), sender.profileImageUrl(), sender.role()));
    }
}
