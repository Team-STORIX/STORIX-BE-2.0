package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
import com.storix.domain.domains.event.domain.AppEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

// 상세 웹페이지 렌더용. 비로그인도 조회하므로 홍보 수단이나 응모권 지급표 같은 운영값은 담지 않는다
@Builder
public record AppEventPageResponse(
        Long id,

        String name,

        String description,

        @Schema(description = "이벤트 종류 (GENERAL / ATTENDANCE / STORY_CARD)")
        AppEventType eventType,

        @Schema(
                description = "그릴 화면을 고르는 키. 같은 종류라도 회차마다 구성이 다를 수 있으니 이 값을 우선 보고, "
                        + "비어 있거나 모르는 값이면 eventType 기준 기본 화면으로 넘어가주세요.",
                example = "attendance-2026-08-10"
        )
        String pageKey,

        @Schema(description = "이벤트 시작 시각", example = "2026-07-01 00:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime startAt,

        @Schema(description = "이벤트 종료 시각(exclusive)", example = "2026-08-01 00:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime endAt,

        @Schema(description = "기간으로 파생 계산되는 상태 (SCHEDULED / ACTIVE / ENDED)")
        AppEventStatus status
) {
    public static AppEventPageResponse from(AppEvent appEvent) {
        return AppEventPageResponse.builder()
                .id(appEvent.getId())
                .name(appEvent.getName())
                .description(appEvent.getDescription())
                .eventType(appEvent.getEventType())
                .pageKey(appEvent.getPageKey())
                .startAt(appEvent.getStartAt())
                .endAt(appEvent.getEndAt())
                .status(AppEventStatus.resolve(appEvent.getStartAt(), appEvent.getEndAt(), LocalDateTime.now()))
                .build();
    }
}
