package com.storix.storix_api.domains.chat.application.port;

import com.storix.storix_api.domains.chat.domain.ChatMessage;
import com.storix.storix_api.domains.chat.dto.ChatMessageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LoadChatPort {
    Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable);
}
