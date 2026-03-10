package com.storix.domain.domains.onboarding.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StandardOnboardingWorksInfo {

    private Long worksId;
    private String worksName;
    private String thumbnailUrl;
    private String artistName;

    public StandardOnboardingWorksInfo(
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

    public static StandardOnboardingWorksInfo of(
            OnboardingWorksInfo raw,
            String artistName
    ) {
        return new StandardOnboardingWorksInfo(
                raw.getWorksId(),
                raw.getWorksName(),
                raw.getThumbnailUrl(),
                artistName
        );
    }
}