package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.plus.domain.Rating;

public record ModifyReviewRequest(
        Rating rating,
        boolean isSpoiler,
        String spoilerScript,
        String content
) {
}
