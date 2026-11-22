package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiChatMessageResponse(
        Long messageId,
        @Schema(description = "USER · CHATBOT")
        AiChatMessageRole role,
        @Schema(description = "TEXT · ONBOARDING")
        AiChatMessageType type,
        String content,
        @Schema(description = "챗봇 답변의 결과 상태. 질문 · 온보딩은 null. 모르는 값은 text 대화로 보여 준다", nullable = true)
        AiChatAnswerStatus status,
        @Schema(description = "추천 작품 카드. 지금은 없어진 작품은 빠진다")
        List<AiChatRecommendedWork> works,
        LocalDateTime createdAt
) {
    public static AiChatMessageResponse of(AiChatMessageInfo message, List<AiChatRawWork> works,
                                           Map<Long, WorksCardInfo> cards, boolean adultVerified) {
        List<AiChatRecommendedWork> recommended = works.stream()
                .filter(work -> cards.containsKey(work.workId()))
                .map(work -> AiChatRecommendedWork.of(work, cards.get(work.workId()), adultVerified))
                .toList();
        return new AiChatMessageResponse(message.messageId(), message.role(), message.type(), message.content(),
                message.status(), recommended, message.createdAt());
    }
}
