package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.works.dto.WorksCardInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatRecommendedWork(
        @Schema(description = "추천 순위. 1부터")
        int rank,
        Long workId,
        String title,
        @Schema(description = "성인 인증 전이면 19금 작품은 null", nullable = true)
        String thumbnailUrl,
        @Schema(description = "첫 번째 플랫폼", example = "카카오페이지", nullable = true)
        String platform,
        @Schema(example = "로판", nullable = true)
        String genre,
        @Schema(description = "19금 작품")
        boolean isAdultOnly,
        @Schema(description = "성인 인증 전이라 표지를 가림. 일반 검색 카드와 같게 그린다")
        boolean isBlinded,
        @Schema(description = "추천 이유")
        String reason,
        @Schema(description = "카드 아래 일치 단서. 표지를 가리면 표지 단서(COVER)도 뺀다")
        List<AiChatMatchedClue> matchedClues
) {
    public static final String COVER_SOURCE = "COVER";

    public static AiChatRecommendedWork of(AiChatRawWork raw, WorksCardInfo card, boolean adultVerified) {
        boolean blinded = card.adult() && !adultVerified;
        List<AiChatMatchedClue> clues = blinded
                ? raw.matchedClues().stream().filter(clue -> !COVER_SOURCE.equals(clue.source())).toList()
                : raw.matchedClues();
        return new AiChatRecommendedWork(raw.rank(), raw.workId(), card.worksName(),
                blinded ? null : card.thumbnailUrl(),
                card.platforms().isEmpty() ? null : card.platforms().get(0).getDbValue(),
                card.genre() == null ? null : card.genre().getDbValue(),
                card.adult(), blinded, raw.reason(), clues);
    }
}
