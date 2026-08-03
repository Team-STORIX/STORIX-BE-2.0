package com.storix.domain.domains.event.dto;

import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;

// 오스카 행운의 작품 후보 조회용
public record StoryCardLuckyWorkPick(
        Long worksId,
        String title,
        WorksType worksType,
        Platform platform,
        String landingUrl
) {
}
