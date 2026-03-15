package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;

public record WorksInfo(
        Long worksId,
        String thumbnailUrl,
        String worksName,
        String artistName,
        WorksType worksType,
        Genre genre
) {
}
