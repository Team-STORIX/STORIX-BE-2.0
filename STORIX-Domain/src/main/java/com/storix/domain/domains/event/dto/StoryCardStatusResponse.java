package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StoryCardStatusResponse(

        Long appEventId,

        @Schema(description = "이벤트 시작일 (yyyy-MM-dd)")
        LocalDate eventStartDate,

        @Schema(description = "이벤트 종료일 (yyyy-MM-dd)")
        LocalDate eventEndDate,

        @Schema(description = "카드 기준일 (06:00 초기화 기준 서비스 날짜)")
        LocalDate serviceDate,

        @Schema(description = "이벤트 진행 중 여부 (기간 외면 false)")
        boolean eventActive,

        @Schema(description = "오늘 카드를 뽑았는지 여부")
        boolean drawnToday,

        @Schema(description = "오늘 뽑은 카드. 아직 안 뽑았으면 null이고, 카드 선택 화면을 띄워주세요")
        StoryCardResponse card
) {
    // 카드 이미지 objectKey → 전체 URL
    public StoryCardStatusResponse withBaseUrl(String baseUrl) {
        if (card == null) {
            return this;
        }
        return StoryCardStatusResponse.builder()
                .appEventId(appEventId)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .serviceDate(serviceDate)
                .eventActive(eventActive)
                .drawnToday(drawnToday)
                .card(card.withBaseUrl(baseUrl))
                .build();
    }
}
