package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.domain.Review;

import java.time.LocalDateTime;

public record ReviewInfo(
        // 유저 정보
        Long reviewerId,

        // 리뷰 정보
        Long reviewId,
        boolean isSpoiler,
        Rating rating,
        String content,
        int likeCount,
        LocalDateTime createdAt,

        // 작품 정보
        Long worksId
) {
    public static ReviewInfo of(Review review) {
        return new ReviewInfo(
                review.getLibraryUserId(),
                review.getId(),
                review.isSpoiler(),
                review.getRating(),
                review.getContent(),
                review.getLikeCount(),
                review.getCreatedAt(),
                review.getWorksId()
        );
    }
}
