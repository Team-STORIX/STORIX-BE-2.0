package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.aichat.dto.AiChatMessagePage;
import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatMessageService {

    private final AiChatAdaptor aiChatAdaptor;
    private final AiChatMessageAdaptor aiChatMessageAdaptor;

    @Transactional
    public AiChatQuestionInfo saveQuestion(Long userId, String question) {
        AiChat chat = aiChatAdaptor.findOrCreate(userId);
        AiChatMessage saved = aiChatMessageAdaptor.save(AiChatMessage.question(chat.getId(), question));
        chat.touch(LocalDateTime.now());
        return new AiChatQuestionInfo(chat.getId(), saved.getId());
    }

    @Transactional
    public Long saveAnswer(Long chatId, String content, AiChatAnswerStatus status,
                           String worksJson, String pipelineVersion) {
        AiChat chat = aiChatAdaptor.findById(chatId);
        AiChatMessage saved = aiChatMessageAdaptor.save(
                AiChatMessage.answer(chatId, content, status, worksJson, pipelineVersion));
        chat.touch(LocalDateTime.now());
        return saved.getId();
    }

    // 질문 직전까지의 대화를 오래된 순으로
    @Transactional(readOnly = true)
    public List<AiChatMessageInfo> findContext(Long chatId, Long beforeMessageId, int limit) {
        List<AiChatMessageInfo> latest = new ArrayList<>(aiChatMessageAdaptor.findContext(chatId, beforeMessageId, limit)
                .stream()
                .map(AiChatMessageInfo::from)
                .toList());
        Collections.reverse(latest);
        return latest;
    }

    @Transactional(readOnly = true)
    public AiChatMessagePage findMessages(Long userId, Long beforeId, int size) {
        return aiChatAdaptor.findByUserId(userId)
                .map(chat -> {
                    List<AiChatMessage> latest = aiChatMessageAdaptor.findLatest(chat.getId(), beforeId, size + 1);
                    boolean hasNext = latest.size() > size;
                    List<AiChatMessageInfo> page = new ArrayList<>(latest.stream()
                            .limit(size)
                            .map(AiChatMessageInfo::from)
                            .toList());
                    Collections.reverse(page);
                    return new AiChatMessagePage(page, hasNext);
                })
                .orElseGet(() -> new AiChatMessagePage(List.of(), false));
    }
}
