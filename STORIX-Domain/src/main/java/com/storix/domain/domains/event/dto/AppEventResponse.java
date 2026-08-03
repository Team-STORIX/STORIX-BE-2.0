package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
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

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime startAt,

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

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AppEventResponse from(AppEvent appEvent) {
        return AppEventResponse.builder()
                .id(appEvent.getId())
                .name(appEvent.getName())
                .description(appEvent.getDescription())
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
