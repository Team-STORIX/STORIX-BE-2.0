package com.storix.domain.domains.chat.adaptor;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatAdaptor {

    private final ChatRepository chatRepository;

    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    public List<ChatMessageResponseDto> loadRecentMessagesBySender(Long roomId, Long senderId, Pageable pageable) {
        return chatRepository.findRecentByRoomIdAndSenderId(roomId, senderId, pageable);
    }

    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }

    public int softDeleteTalkMessagesBySender(Long roomId, Long senderId) {
        return chatRepository.softDeleteByRoomIdAndSenderId(roomId, senderId, MessageType.TALK, LocalDateTime.now());
    }

    public int hardDeleteBefore(LocalDateTime cutoff) {
        return chatRepository.hardDeleteBefore(cutoff);
    }

    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, List<Long> blockedIds, Pageable pageable) {
        if (blockedIds.isEmpty()) {
            return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }
        return chatRepository.findAllByRoomIdExcludingBlockedOrderByCreatedAtDesc(roomId, blockedIds, pageable);
    }
}
