package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.ContentTargetType;

import java.time.LocalDateTime;

public record BannerCommand(
        Long appEventId,
        ContentTargetType contentTargetType,
        String bannerTitle,
        String imageObjectKey,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt
) {
}
