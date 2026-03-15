package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.domain.Genre;

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
