package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.event.domain.PromotionType;

import java.time.LocalDateTime;
import java.util.Set;

public record AppEventCommand(
        String name,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean hasWinner,
        Set<PromotionType> promotionTypes
) {
}
