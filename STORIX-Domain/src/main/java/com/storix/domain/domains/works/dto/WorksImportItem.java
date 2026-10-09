package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksImportItem(
        // 검수 대기 작품
        Long stagingId, // 크롤러 검수 DB id
        String worksName,
        String artistName,
        String author,
        String illustrator,
        String originalAuthor,
        String ageClassification,
        String genre,
        String worksType,
        String platform,
        String landingUrl,
        String description,
        String thumbnailUrl,
        List<String> hashtags,

        // 재검수 대기 작품
        Long targetWorksId, // 병합 대상 작품 id
        boolean createNew // 판정 스킵
) {
}
