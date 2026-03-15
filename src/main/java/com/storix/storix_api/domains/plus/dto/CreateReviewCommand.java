package com.storix.storix_api.domains.plus.dto;

import com.storix.storix_api.domains.plus.domain.Rating;
import com.storix.storix_api.domains.plus.domain.Review;

public record CreateReviewCommand(
        Long libraryUserId,
        Long worksId,
        boolean isSpoiler,
        Rating rating,
        String content
) {
    public Review toEntity() {
        return new Review(
                libraryUserId,
                worksId,
                isSpoiler,
                rating,
                content
        );
    }
}
