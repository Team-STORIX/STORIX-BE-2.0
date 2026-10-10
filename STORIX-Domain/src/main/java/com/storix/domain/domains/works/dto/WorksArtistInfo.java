package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.WorksType;

public record WorksArtistInfo(
        Long worksId,
        WorksType worksType,
        String originalAuthor,
        String author,
        String illustrator,
        String artistName
) {
}
