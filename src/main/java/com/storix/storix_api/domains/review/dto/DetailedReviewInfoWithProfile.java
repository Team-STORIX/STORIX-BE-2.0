package com.storix.storix_api.domains.review.dto;

import com.storix.storix_api.domains.user.dto.StandardProfileInfo;
import com.storix.storix_api.domains.works.dto.StandardWorksInfo;

public record DetailedReviewInfoWithProfile(
        // 유저 프로필
        StandardProfileInfo profile,

        // 작품 정보
        StandardWorksInfo works,

        // 리뷰 정보
        StandardReviewInfo review
) {
    public static DetailedReviewInfoWithProfile of(
            StandardProfileInfo profile,
            StandardWorksInfo works,
            StandardReviewInfo review
    ) {
        return new DetailedReviewInfoWithProfile(
                profile,
                works,
                review
        );
    }
}
