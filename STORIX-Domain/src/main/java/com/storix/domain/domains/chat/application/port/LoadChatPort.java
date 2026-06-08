package com.storix.domain.domains.chat.application.port;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface LoadChatPort {
    Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable);

    List<ChatMessageResponseDto> loadRecentMessagesBySender(Long roomId, Long senderId, Pageable pageable);
}
