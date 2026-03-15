package com.storix.storix_api.domains.plus.dto;

public record SliceReviewInfo(
        Long userId,
        Long reviewId,
        boolean isSpoiler,
        String content
) {
}
