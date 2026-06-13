package com.storix.domain.domains.chat.application.port;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface LoadChatPort {
    Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable);
    Slice<ChatMessageResponseDto> loadMessages(Long roomId, List<Long> blockedIds, Pageable pageable);
}
