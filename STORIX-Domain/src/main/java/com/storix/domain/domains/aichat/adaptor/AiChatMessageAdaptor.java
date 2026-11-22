package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
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

    // AI 서버에 넘길 대화. 온보딩 · 오류 답변은 빼고 최신순
    public List<AiChatMessage> findContext(Long chatId, Long beforeId, int limit) {
        return aiChatMessageRepository.findContext(chatId, beforeId, AiChatMessageType.TEXT,
                AiChatAnswerStatus.ERROR, Limit.of(limit));
    }

    public int deleteAllByChatId(Long chatId) {
        return aiChatMessageRepository.deleteAllByChatId(chatId);
    }
}
