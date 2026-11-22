package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.List;
import java.util.Set;

public record AiChatQuestion(
        Long userId,
        Long chatId,
        Long messageId,
        String question,
        // 설정 켜짐 + 지금 성인 인증 유효일 때만 true
        boolean adultAllowed,
        Set<WorksType> worksTypes,
        // 서재 작품. 추천 제외
        List<Long> excludedWorkIds,
        AiChatContext context
) {
    public static AiChatQuestion of(Long userId, AiChatQuestionInfo saved, String question,
                                    AiChatSettingInfo setting, boolean adultVerified,
                                    // 서재 작품. 추천 제외
        List<Long> excludedWorkIds, AiChatContext context) {
        return new AiChatQuestion(
                userId,
                saved.chatId(),
                saved.messageId(),
                question,
                setting.adultEnabled() && adultVerified,
                setting.worksTypes(),
                excludedWorkIds,
                context
        );
    }
}
