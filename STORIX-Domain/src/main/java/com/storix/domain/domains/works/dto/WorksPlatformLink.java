package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.WorksPlatform;

public record WorksPlatformLink(
        String platform,
        String landingUrl
) {
    public static WorksPlatformLink from(WorksPlatform worksPlatform) {
        return new WorksPlatformLink(worksPlatform.getPlatform().getDbValue(), worksPlatform.getLandingUrl());
    }
}
