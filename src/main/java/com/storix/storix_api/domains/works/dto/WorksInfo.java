package com.storix.storix_api.domains.works.dto;

import com.storix.storix_api.domains.works.domain.Genre;
import com.storix.storix_api.domains.works.domain.WorksType;

public record WorksInfo(
        Long worksId,
        String thumbnailUrl,
        String worksName,
        String artistName,
        WorksType worksType,
        Genre genre
) {
}
