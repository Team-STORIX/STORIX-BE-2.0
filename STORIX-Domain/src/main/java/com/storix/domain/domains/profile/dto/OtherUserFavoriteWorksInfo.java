package com.storix.domain.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.dto.WorksInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtherUserFavoriteWorksInfo(
        Long worksId,
        String worksName,
        String artistName,
        String thumbnailUrl,
        String worksType,
        boolean isAdultOnly,
        boolean isBlinded
) {
    public static OtherUserFavoriteWorksInfo of(WorksInfo base, boolean excludeAdult) {
        boolean isAdultOnly = AdultContentPolicy.isAdultOnly(base.ageClassification());
        boolean isBlinded = isAdultOnly && excludeAdult;

        return new OtherUserFavoriteWorksInfo(
                base.worksId(),
                base.worksName(),
                base.artistName(),
                isBlinded ? null : base.thumbnailUrl(),
                base.worksType() != null ? base.worksType().getDbValue() : null,
                isAdultOnly,
                isBlinded
        );
    }
}
