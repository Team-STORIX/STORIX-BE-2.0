package com.storix.domain.domains.works.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.AgeClassification;

public record SlicedWorksInfo(
        Long worksId,
        String thumbnailUrl,
        String worksName,
        AgeClassification ageClassification
) {
    // record 정식 컴포넌트가 아닌 파생 메서드라 @JsonProperty 없이는 "adultOnly"로 직렬화된다
    @JsonProperty("isAdultOnly")
    public boolean isAdultOnly() {
        return AdultContentPolicy.isAdultOnly(ageClassification);
    }
}
