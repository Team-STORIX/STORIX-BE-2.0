package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import lombok.Builder;

@Builder
public record StandardSliceReviewInfo(
        Long reviewId,
        boolean isSpoiler,
        String spoilerScript,
        String content,
        Rating rating
) {
    public static StandardSliceReviewInfo from(SliceReviewInfo review) {
        return StandardSliceReviewInfo.builder()
                .reviewId(review.reviewId())
                .isSpoiler(review.isSpoiler())
                .spoilerScript(review.spoilerScript())
                .content(review.content())
                .rating(review.rating())
                .build();
    }
}
