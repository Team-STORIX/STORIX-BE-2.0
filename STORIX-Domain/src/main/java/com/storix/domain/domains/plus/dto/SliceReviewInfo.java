package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.Rating;

public record SliceReviewInfo(
        Long userId,
        Long reviewId,
        boolean isSpoiler,
        String spoilerScript,
        String content,
        Rating rating
) {
}
