package com.storix.domain.domains.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingWorksInfo {

    private Long worksId;
    private String worksName;
    private String thumbnailUrl;
    private String artistName;

    public OnboardingWorksInfo(
            Long worksId,
            String worksName,
            String thumbnailUrl,
            String artistName
    ) {
        this.worksId = worksId;
        this.worksName = worksName;
        this.thumbnailUrl = thumbnailUrl;
        this.artistName = artistName;
    }
}
