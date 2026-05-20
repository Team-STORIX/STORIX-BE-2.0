package com.storix.domain.domains.chat.adaptor;

import com.storix.domain.domains.chat.application.port.LoadChatPort;
import com.storix.domain.domains.chat.application.port.RecordChatPort;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatPersistenceAdapter implements RecordChatPort, LoadChatPort {

    private final ChatRepository chatRepository;

    @Override
    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    public List<ChatMessageResponseDto> loadRecentMessagesBySender(Long roomId, Long senderId, Pageable pageable) {
        return chatRepository.findRecentByRoomIdAndSenderId(roomId, senderId, pageable);
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }
}
