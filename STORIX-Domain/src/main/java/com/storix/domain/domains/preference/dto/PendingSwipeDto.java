package com.storix.domain.domains.preference.dto;

import lombok.Builder;

@Builder
public record PendingSwipeDto(
        Long userId,
        Long worksId,
        boolean isLiked
) {
}
