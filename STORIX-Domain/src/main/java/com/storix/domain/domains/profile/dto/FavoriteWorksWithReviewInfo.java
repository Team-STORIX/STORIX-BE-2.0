package com.storix.domain.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
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
        String rating,

        // 성인 작품 여부
        boolean isAdultOnly,
        boolean isBlinded
) {
    public static FavoriteWorksWithReviewInfo of(WorksInfo base, boolean isReviewed, String rating, boolean excludeAdult) {
        boolean isAdultOnly = AdultContentPolicy.isAdultOnly(base.ageClassification());
        boolean isBlinded = isAdultOnly && excludeAdult;

        return new FavoriteWorksWithReviewInfo(
                base.worksId(),
                base.worksName(),
                base.artistName(),
                isBlinded ? null : base.thumbnailUrl(),
                base.worksType() != null ? base.worksType().getDbValue() : null,
                isReviewed,
                rating,
                isAdultOnly,
                isBlinded
        );
    }
}
