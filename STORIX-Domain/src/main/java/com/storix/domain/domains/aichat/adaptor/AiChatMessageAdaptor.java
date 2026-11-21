package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.repository.AiChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiChatMessageAdaptor {

    private final AiChatMessageRepository aiChatMessageRepository;

    public AiChatMessage save(AiChatMessage message) {
        return aiChatMessageRepository.save(message);
    }

    // 최신순. beforeId 가 없으면 가장 최근부터
    public List<AiChatMessage> findLatest(Long chatId, Long beforeId, int limit) {
        if (beforeId == null) {
            return aiChatMessageRepository.findByChatIdOrderByIdDesc(chatId, Limit.of(limit));
        }
        return aiChatMessageRepository.findByChatIdAndIdLessThanOrderByIdDesc(chatId, beforeId, Limit.of(limit));
    }

    public int deleteAllByChatId(Long chatId) {
        return aiChatMessageRepository.deleteAllByChatId(chatId);
    }
}
