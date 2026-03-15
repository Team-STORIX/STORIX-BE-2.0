package com.storix.domain.domains.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingWorksInfo {

    // 작품 정보
    private Long worksId;
    private String worksName;
    private String thumbnailUrl;

    // 작가 정보
    private String author;
    private String illustrator;
    private String originalAuthor;

    public OnboardingWorksInfo(
            Long worksId,
            String worksName,
            String thumbnailUrl,
            String author,
            String illustrator,
            String originalAuthor
    ) {
        this.worksId = worksId;
        this.worksName = worksName;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.illustrator = illustrator;
        this.originalAuthor = originalAuthor;
    }
}