package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksImportItem(
        Long stagingId,
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
        List<String> hashtags
) {
}
