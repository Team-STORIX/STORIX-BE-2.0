package com.storix.storix_api.domains.review.dto;

import com.storix.storix_api.domains.user.dto.StandardProfileInfo;

public record SliceReviewInfoWithProfile(
        // 유저 프로필
        StandardProfileInfo profile,

        // 리뷰 정보
        StandardSliceReviewInfo review
) {
    public static SliceReviewInfoWithProfile of(StandardSliceReviewInfo review, StandardProfileInfo profile) {
        return new SliceReviewInfoWithProfile(
                profile,
                review
        );
    }
}
