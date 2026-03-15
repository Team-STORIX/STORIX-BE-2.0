package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.works.dto.WorksInfo;

import java.util.List;

public record BoardWorksInfo(
        // 작품 정보
        Long worksId,
        String thumbnailUrl,
        String worksName,
        String artistName,
        String worksType,
        String genre,

        // 해시태그 정보
        List<String> hashtags
) {
    public static BoardWorksInfo from(
            WorksInfo works,
            List<String> hashtags
    ) {
        if (works == null) return null;

        return new BoardWorksInfo(
                works.worksId(),
                works.thumbnailUrl(),
                works.worksName(),
                works.artistName(),
                works.worksType().getDbValue(),
                works.genre().getDbValue(),
                hashtags
        );
    }
}