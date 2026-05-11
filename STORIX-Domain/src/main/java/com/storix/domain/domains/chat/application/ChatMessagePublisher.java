package com.storix.domain.domains.chat.application;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;

public interface ChatMessagePublisher {
    void publish(ChatMessageResponseDto response);
}
