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
    // 동시에 처음 만들어도 INSERT 충돌 예외 없이 같은 행을 돌려준다. 트랜잭션 안에서 불러야 한다
    public AiChat findOrCreate(Long userId) {
        return aiChatRepository.findByUserId(userId)
                .orElseGet(() -> {
                    aiChatRepository.insertIfAbsent(userId);
                    return aiChatRepository.findByUserIdForUpdate(userId)
                            .orElseThrow(() -> AiChatNotFoundException.EXCEPTION);
                });
    }

    public int deleteById(Long chatId) {
        return aiChatRepository.deleteByIdDirectly(chatId);
    }
}
