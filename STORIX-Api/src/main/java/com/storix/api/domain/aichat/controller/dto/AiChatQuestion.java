package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.Set;

public record AiChatQuestion(
        Long userId,
        Long chatId,
        Long messageId,
        String question,
        boolean adultDefault,
        boolean adultVerified,
        Set<WorksType> worksTypes
) {
    public static AiChatQuestion of(Long userId, AiChatQuestionInfo saved, String question,
                                    AiChatSettingInfo setting, boolean adultVerified) {
        return new AiChatQuestion(
                userId,
                saved.chatId(),
                saved.messageId(),
                question,
                setting.adultEnabled() && adultVerified,
                adultVerified,
                setting.worksTypes()
        );
    }
}
