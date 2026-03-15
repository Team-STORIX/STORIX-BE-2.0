package com.storix.storix_api.domains.chat.adaptor;

import com.storix.storix_api.domains.chat.application.port.LoadChatPort;
import com.storix.storix_api.domains.chat.application.port.RecordChatPort;
import com.storix.storix_api.domains.chat.domain.ChatMessage;
import com.storix.storix_api.domains.chat.dto.ChatMessageResponseDto;
import com.storix.storix_api.domains.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatPersistenceAdapter implements RecordChatPort, LoadChatPort {

    private final ChatRepository chatRepository;

    @Override
    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }
}