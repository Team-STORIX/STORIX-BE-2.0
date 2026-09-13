package com.storix.domain.domains.library.dto;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;

public record StandardLibraryWorksInfo(
        // 작품 정보
        Long worksId,
        String worksName,
        String artistName,
        String thumbnailUrl,
        String worksType,
        String genre,

        // 성인 작품 여부
        boolean isAdultOnly,

        // 요청 유저의 성인 인증이 유효하지 않아 서버가 정보를 가렸는지 여부
        boolean isBlinded,

        // 리뷰 정보
        Long reviewId,
        String rating
) {
    public static StandardLibraryWorksInfo of(
            LibraryWorksInfo worksInfo,
            String artistName,
            Long reviewId,
            Rating rating,
            boolean excludeAdult
    ) {
        boolean isAdultOnly = AdultContentPolicy.isAdultOnly(worksInfo.ageClassification());
        boolean isBlinded = isAdultOnly && excludeAdult;

        return new StandardLibraryWorksInfo(
                // 작품 정보
                worksInfo.worksId(),
                worksInfo.worksName(),
                artistName,
                isBlinded ? null : worksInfo.thumbnailUrl(),
                worksInfo.worksType() != null ? worksInfo.worksType().getDbValue() : null,
                worksInfo.genre() != null ? worksInfo.genre().getDbValue() : null,
                isAdultOnly,
                isBlinded,

                // 리뷰 정보
                reviewId,
                rating != null ? rating.getDbValue() : null
        );
    }
}
