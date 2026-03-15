package com.storix.storix_api.domains.works.dto;

import com.storix.storix_api.domains.works.domain.WorksType;
import com.storix.storix_api.domains.works.domain.Genre;

public record LibraryWorksInfo(
        // 작품 정보
        Long worksId,
        String worksName,
        String author,
        String illustrator,
        String originalAuthor,
        String thumbnailUrl,
        WorksType worksType,
        Genre genre
) {
}
