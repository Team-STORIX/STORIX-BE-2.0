package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.works.dto.WorksCardInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatRecommendedWork(
        @Schema(description = "추천 순위. 1부터")
        int rank,
        Long workId,
        String title,
        @Schema(nullable = true)
        String thumbnailUrl,
        @Schema(description = "첫 번째 플랫폼", example = "카카오페이지", nullable = true)
        String platform,
        @Schema(example = "로판", nullable = true)
        String genre,
        @Schema(description = "추천 이유")
        String reason,
        @Schema(description = "카드 아래 일치 단서")
        List<AiChatMatchedClue> matchedClues
) {
    public static AiChatRecommendedWork of(AiChatRawWork raw, WorksCardInfo card) {
        return new AiChatRecommendedWork(raw.rank(), raw.workId(), card.worksName(), card.thumbnailUrl(),
                card.platforms().isEmpty() ? null : card.platforms().get(0).getDbValue(),
                card.genre() == null ? null : card.genre().getDbValue(),
                raw.reason(), raw.matchedClues());
    }
}
