package com.storix.domain.domains.preference.dto;

import java.time.LocalDateTime;

public record ExplorationReactionWithCreatedAt(
        Long worksId,
        boolean isLiked,
        LocalDateTime createdAt
) {
}
