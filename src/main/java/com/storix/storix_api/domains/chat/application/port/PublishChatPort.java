package com.storix.storix_api.domains.chat.application.port;

import com.storix.storix_api.domains.chat.dto.ChatMessageResponseDto;

public interface PublishChatPort {
    void publish(ChatMessageResponseDto response);
}
