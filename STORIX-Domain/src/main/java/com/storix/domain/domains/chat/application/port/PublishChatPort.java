package com.storix.domain.domains.chat.application.port;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;

public interface PublishChatPort {
    void publish(ChatMessageResponseDto response);
}
