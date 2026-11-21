package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatSettingAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 테스터가 온보딩부터 다시 보도록 대화 기록 · 대화방 · 설정을 지운다
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatResetService {

    private final AiChatAdaptor aiChatAdaptor;
    private final AiChatMessageAdaptor aiChatMessageAdaptor;
    private final AiChatSettingAdaptor aiChatSettingAdaptor;

    @Transactional
    public void reset(Long userId) {
        int deletedMessages = aiChatAdaptor.findByUserId(userId)
                .map(chat -> {
                    int deleted = aiChatMessageAdaptor.deleteAllByChatId(chat.getId());
                    aiChatAdaptor.deleteById(chat.getId());
                    return deleted;
                })
                .orElse(0);
        int deletedSettings = aiChatSettingAdaptor.deleteByUserId(userId);

        log.warn(">>> [AiChat] 테스터 초기화 userId={} deletedMessages={} deletedSettings={}",
                userId, deletedMessages, deletedSettings);
    }
}
