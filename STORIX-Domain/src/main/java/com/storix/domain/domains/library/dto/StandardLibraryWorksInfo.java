package com.storix.domain.domains.library.dto;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;

public record StandardLibraryWorksInfo(
        // 작품 정보
        Long worksId,
        String worksName,
        String artistName,
        String thumbnailUrl,
        String worksType,
        String genre,

        // 리뷰 정보
        Long reviewId,
        String rating
) {
    public static StandardLibraryWorksInfo of(
            LibraryWorksInfo worksInfo,
            String artistName,
            Long reviewId,
            Rating rating
    ) {
        return new StandardLibraryWorksInfo(
                // 작품 정보
                worksInfo.worksId(),
                worksInfo.worksName(),
                artistName,
                worksInfo.thumbnailUrl(),
                worksInfo.worksType() != null ? worksInfo.worksType().getDbValue() : null,
                worksInfo.genre() != null ? worksInfo.genre().getDbValue() : null,

                // 리뷰 정보
                reviewId,
                rating != null ? rating.getDbValue() : null
        );
    }
}
