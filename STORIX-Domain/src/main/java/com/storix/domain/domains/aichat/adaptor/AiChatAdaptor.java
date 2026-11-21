package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.repository.AiChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiChatAdaptor {

    private final AiChatRepository aiChatRepository;

    public AiChat save(AiChat aiChat) {
        return aiChatRepository.save(aiChat);
    }

    public Optional<AiChat> findByUserId(Long userId) {
        return aiChatRepository.findByUserId(userId);
    }

    public int deleteById(Long chatId) {
        return aiChatRepository.deleteByIdDirectly(chatId);
    }
}
