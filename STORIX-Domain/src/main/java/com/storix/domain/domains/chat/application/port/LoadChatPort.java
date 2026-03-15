package com.storix.domain.domains.chat.application.port;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface LoadChatPort {
    Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable);
}
