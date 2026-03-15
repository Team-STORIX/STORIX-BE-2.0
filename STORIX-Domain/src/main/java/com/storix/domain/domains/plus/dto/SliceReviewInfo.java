package com.storix.domain.domains.plus.dto;

public record SliceReviewInfo(
        Long userId,
        Long reviewId,
        boolean isSpoiler,
        String content
) {
}
