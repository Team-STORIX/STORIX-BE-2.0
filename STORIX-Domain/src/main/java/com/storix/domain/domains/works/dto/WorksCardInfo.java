package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksPlatform;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.List;
import java.util.Objects;

// 추천 카드에 쓰는 작품 정보. 플랫폼은 작품 상세와 같은 순서(enum 선언 순)
public record WorksCardInfo(
        Long worksId,
        String worksName,
        String thumbnailUrl,
        WorksType worksType,
        Genre genre,
        List<Platform> platforms,
        boolean adult
) {
    public static WorksCardInfo from(Works works) {
        List<Platform> platforms = works.getPlatforms().stream()
                .map(WorksPlatform::getPlatform)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        return new WorksCardInfo(works.getId(), works.getWorksName(), works.getThumbnailUrl(),
                works.getWorksType(), works.getGenre(), platforms,
                works.getAgeClassification() == AgeClassification.AGE_18);
    }
}
