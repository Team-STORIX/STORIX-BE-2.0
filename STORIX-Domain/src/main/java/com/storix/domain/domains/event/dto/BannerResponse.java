package com.storix.domain.domains.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.domain.BannerStatus;
import com.storix.domain.domains.event.domain.ContentTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record BannerResponse(
        Long id,

        @JsonProperty("targetId")
        @Schema(description = "소속 앱 이벤트 id, 없으면 null. 앱은 storix.kr/event/{targetId}로 이동")
        Long appEventId,

        @Schema(description = "콘텐츠 유형")
        ContentTargetType contentTargetType,

        String bannerTitle,

        String imageUrl,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayStartAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime displayEndAt,

        @Schema(description = "배너 상태")
        BannerStatus status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static BannerResponse from(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .appEventId(banner.getAppEvent() == null ? null : banner.getAppEvent().getId())
                .contentTargetType(banner.getContentTargetType())
                .bannerTitle(banner.getBannerTitle())
                .imageUrl(banner.getImageObjectKey())
                .displayStartAt(banner.getDisplayStartAt())
                .displayEndAt(banner.getDisplayEndAt())
                .status(banner.getStatus())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    // objectKey → 전체 URL로 변환
    public BannerResponse withBaseUrl(String baseUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return this;
        }
        return this.toBuilder()
                .imageUrl(baseUrl + "/" + imageUrl)
                .build();
    }
}
