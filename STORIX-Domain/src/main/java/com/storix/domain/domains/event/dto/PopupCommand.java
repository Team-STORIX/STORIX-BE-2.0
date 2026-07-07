package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;

import java.time.LocalDateTime;

public record PopupCommand(
        Long appEventId,
        ContentTargetType contentTargetType,
        PopupExposurePolicy exposurePolicy,
        String popupTitle,
        String imageObjectKey,
        String content,
        String ctaText,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt
) {
}
