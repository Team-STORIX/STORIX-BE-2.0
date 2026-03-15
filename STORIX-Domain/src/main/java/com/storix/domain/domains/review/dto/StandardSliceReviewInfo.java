package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.plus.dto.SliceReviewInfo;

public record StandardSliceReviewInfo(
        Long reviewId,
        boolean isSpoiler,
        String content
) {
    public static StandardSliceReviewInfo from(SliceReviewInfo review) {
        return new StandardSliceReviewInfo(
                review.reviewId(),
                review.isSpoiler(),
                review.content()
        );
    }
}
