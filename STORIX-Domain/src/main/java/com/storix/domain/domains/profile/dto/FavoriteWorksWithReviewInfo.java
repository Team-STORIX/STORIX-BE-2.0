package com.storix.domain.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.works.dto.WorksInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FavoriteWorksWithReviewInfo(
        // 작품 정보
        Long worksId,
        String worksName,
        String artistName,
        String thumbnailUrl,
        String worksType,

        // 리뷰 정보
        boolean isReviewed,
        String rating
) {
    public static FavoriteWorksWithReviewInfo of(WorksInfo base, boolean isReviewed, String rating) {
        return new FavoriteWorksWithReviewInfo(
                base.worksId(),
                base.worksName(),
                base.artistName(),
                base.thumbnailUrl(),
                base.worksType().getDbValue(),
                isReviewed,
                rating
        );
    }
}
