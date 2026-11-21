package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.Set;

// 답변을 만들 때 넘기는 질문과 정책. adultAllowed 는 설정 켜짐 + 지금 성인 인증 유효일 때만 true
public record AiChatQuestion(
        Long userId,
        Long chatId,
        Long messageId,
        String question,
        boolean adultAllowed,
        Set<WorksType> worksTypes
) {
    public static AiChatQuestion of(Long userId, AiChatQuestionInfo saved, String question,
                                    AiChatSettingInfo setting, boolean adultVerified) {
        return new AiChatQuestion(userId, saved.chatId(), saved.messageId(), question,
                setting.adultEnabled() && adultVerified, setting.worksTypes());
    }
}
