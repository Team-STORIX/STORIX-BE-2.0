package com.storix.api.domain.event.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;
import com.storix.domain.domains.event.dto.PopupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PopupRequest(
        @Schema(description = "소속 앱 이벤트 id (선택). 지정 시 노출기간이 이벤트 기간 안으로 제한, 수정 시 무시", example = "10")
        Long appEventId,

        @Schema(description = "콘텐츠 유형", example = "APP_EVENT")
        @NotNull(message = "콘텐츠 유형은 필수입니다.")
        ContentTargetType contentTargetType,

        @Schema(description = "노출 정책 (ALWAYS_DURING_PERIOD: 닫기만 가능, ONCE_PER_DAY: 오늘 다시 보지 않기)", example = "ONCE_PER_DAY")
        @NotNull(message = "노출 정책은 필수입니다.")
        PopupExposurePolicy exposurePolicy,

        @Schema(description = "팝업 제목", example = "여름맞이 이벤트")
        @NotBlank(message = "팝업 제목은 필수입니다.")
        @Size(max = 100, message = "팝업 제목은 100자 이하여야 합니다.")
        String popupTitle,

        @Schema(description = "팝업 내용", example = "지금 참여하고 혜택을 받아보세요!")
        @Size(max = 500, message = "팝업 내용은 500자 이하여야 합니다.")
        String content,

        @Schema(description = "CTA 버튼 텍스트", example = "자세히 보기")
        @Size(max = 40, message = "CTA 텍스트는 40자 이하여야 합니다.")
        String ctaText,

        @Schema(description = "노출 시작 시각", example = "2026-06-25 00:00")
        @NotNull(message = "노출 시작 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayStartAt,

        @Schema(description = "노출 종료 시각", example = "2026-06-30 23:59")
        @NotNull(message = "노출 종료 일시는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayEndAt
) {
    @AssertTrue(message = "노출 종료 일시는 시작 일시 이후여야 합니다.")
    private boolean isDisplayPeriodValid() {
        if (displayStartAt == null || displayEndAt == null) return true; // 부재는 @NotNull 이 담당
        return displayStartAt.isBefore(displayEndAt);
    }

    @AssertTrue(message = "APP_EVENT 유형 팝업은 앱 이벤트 id(appEventId)가 필수입니다.")
    private boolean isAppEventProvidedForAppEventType() {
        if (contentTargetType != ContentTargetType.APP_EVENT) return true; // 다른 유형은 이벤트 무관
        return appEventId != null;
    }

    public PopupCommand toCommand(String imageObjectKey) {
        return new PopupCommand(appEventId, contentTargetType, exposurePolicy, popupTitle, imageObjectKey, content, ctaText, displayStartAt, displayEndAt);
    }
}
