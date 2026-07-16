package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;
import com.storix.domain.domains.event.domain.PopupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record PopupResponse(
        Long id,

        @JsonProperty("targetId")
        @Schema(description = "소속 앱 이벤트 id, 없으면 null. 앱은 storix.kr/event/{targetId}로 이동")
        Long appEventId,

        @Schema(description = "콘텐츠 유형")
        ContentTargetType contentTargetType,

        @Schema(description = "노출 정책 (ALWAYS_DURING_PERIOD: 닫기만 가능, ONCE_PER_DAY: 오늘 다시 보지 않기)")
        PopupExposurePolicy exposurePolicy,

        String popupTitle,

        String imageUrl,

        String content,

        String ctaText,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayStartAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayEndAt,

        @Schema(description = "팝업 상태")
        PopupStatus status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static PopupResponse from(Popup popup) {
        return PopupResponse.builder()
                .id(popup.getId())
                .appEventId(popup.getAppEvent() == null ? null : popup.getAppEvent().getId())
                .contentTargetType(popup.getContentTargetType())
                .exposurePolicy(popup.getExposurePolicy())
                .popupTitle(popup.getPopupTitle())
                .imageUrl(popup.getImageObjectKey())
                .content(popup.getContent())
                .ctaText(popup.getCtaText())
                .displayStartAt(popup.getDisplayStartAt())
                .displayEndAt(popup.getDisplayEndAt())
                .status(popup.getStatus())
                .createdAt(popup.getCreatedAt())
                .updatedAt(popup.getUpdatedAt())
                .build();
    }

    // objectKey → 전체 URL로 변환
    public PopupResponse withBaseUrl(String baseUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return this;
        }
        return this.toBuilder()
                .imageUrl(baseUrl + "/" + imageUrl)
                .build();
    }
}
