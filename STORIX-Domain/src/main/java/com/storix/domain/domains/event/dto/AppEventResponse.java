package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.domain.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Builder
public record AppEventResponse(
        Long id,

        String name,

        String description,

        @Schema(description = "이벤트 상세 웹페이지가 그릴 화면을 고르는 키. 비어 있으면 종류별 기본 화면", example = "attendance-2026-08-10")
        String pageKey,

        @Schema(description = "이벤트 종류 (GENERAL / ATTENDANCE / STORY_CARD)")
        AppEventType eventType,

        @Schema(description = "이벤트 시작 시각", example = "2026-07-01 00:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime startAt,

        @Schema(description = "이벤트 종료 시각(exclusive)", example = "2026-08-01 00:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime endAt,

        @Schema(description = "기간으로 파생 계산되는 상태 (DB 저장값 아님)")
        AppEventStatus status,

        @Schema(description = "EVENT_WINNERS 알림 대상 여부")
        boolean hasWinner,

        @Schema(description = "홍보 수단, 다중 선택 (PUSH / POPUP / BANNER)")
        Set<PromotionType> promotionTypes,

        @Schema(description = "출석 이벤트 응모권 지급 기준 (키=누적 출석일, 값=누적 지급 응모권). 미지정 시 빈 값")
        Map<Integer, Integer> attendanceRewards,

        @Schema(example = "2026-06-20 10:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime createdAt,

        @Schema(example = "2026-06-20 10:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AppEventResponse from(AppEvent appEvent) {
        return AppEventResponse.builder()
                .id(appEvent.getId())
                .name(appEvent.getName())
                .description(appEvent.getDescription())
                .pageKey(appEvent.getPageKey())
                .eventType(appEvent.getEventType())
                .startAt(appEvent.getStartAt())
                .endAt(appEvent.getEndAt())
                .status(AppEventStatus.resolve(appEvent.getStartAt(), appEvent.getEndAt(), LocalDateTime.now()))
                .hasWinner(appEvent.isHasWinner())
                .promotionTypes(Set.copyOf(appEvent.getPromotionTypes()))
                .attendanceRewards(Map.copyOf(appEvent.getAttendanceRewards()))
                .createdAt(appEvent.getCreatedAt())
                .updatedAt(appEvent.getUpdatedAt())
                .build();
    }
}
