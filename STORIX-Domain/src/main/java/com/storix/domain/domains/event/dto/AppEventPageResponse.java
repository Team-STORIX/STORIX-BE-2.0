package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventStatus;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.domain.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

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
        AppEventStatus status,

        @Schema(
                description = "이 이벤트에 설정된 홍보 수단 중 웹페이지가 알아야 하는 것 (POPUP / BANNER). "
                        + "실제로 띄울 대상이 있는지는 popupId / bannerId 로 판단해주세요.",
                example = "[\"BANNER\"]"
        )
        Set<PromotionType> promotionTypes,

        @Schema(description = "이 이벤트에 걸린 노출 중인 팝업 id. 없으면 null", example = "12")
        Long popupId,

        @Schema(
                description = "이 이벤트에 걸린 노출 중인 배너 id. 없으면 null. "
                        + "이 값으로 GET /api/v1/app-events/banner/{bannerId}/modal-required 를 호출해 안내 모달 노출 여부를 판단하세요.",
                example = "34"
        )
        Long bannerId
) {
    private static final Set<PromotionType> WEB_VISIBLE_PROMOTION_TYPES =
            Set.of(PromotionType.POPUP, PromotionType.BANNER);

    public static AppEventPageResponse from(AppEvent appEvent, Long popupId, Long bannerId) {
        return AppEventPageResponse.builder()
                .id(appEvent.getId())
                .name(appEvent.getName())
                .description(appEvent.getDescription())
                .eventType(appEvent.getEventType())
                .pageKey(appEvent.getPageKey())
                .startAt(appEvent.getStartAt())
                .endAt(appEvent.getEndAt())
                .status(AppEventStatus.resolve(appEvent.getStartAt(), appEvent.getEndAt(), LocalDateTime.now()))
                .promotionTypes(appEvent.getPromotionTypes().stream()
                        .filter(WEB_VISIBLE_PROMOTION_TYPES::contains)
                        .collect(Collectors.toUnmodifiableSet()))
                .popupId(popupId)
                .bannerId(bannerId)
                .build();
    }
}
