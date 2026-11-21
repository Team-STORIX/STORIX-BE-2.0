package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.exception.AiChatNotFoundException;
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

    public AiChat findById(Long chatId) {
        return aiChatRepository.findById(chatId)
                .orElseThrow(() -> AiChatNotFoundException.EXCEPTION);
    }

    // 대화방은 사용자당 하나. 온보딩 저장 · 첫 질문 때 만든다
    public AiChat findOrCreate(Long userId) {
        return aiChatRepository.findByUserId(userId)
                .orElseGet(() -> aiChatRepository.save(AiChat.of(userId)));
    }

    public int deleteById(Long chatId) {
        return aiChatRepository.deleteByIdDirectly(chatId);
    }
}
