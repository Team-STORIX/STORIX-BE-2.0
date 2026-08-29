package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.AgeClassification;

public record SlicedWorksInfo(
        Long worksId,
        String thumbnailUrl,
        String worksName,
        AgeClassification ageClassification
) {
    public boolean isAdultOnly() {
        return AdultContentPolicy.isAdultOnly(ageClassification);
    }
}
