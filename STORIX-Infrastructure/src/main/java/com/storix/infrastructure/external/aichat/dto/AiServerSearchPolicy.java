package com.storix.infrastructure.external.aichat.dto;

import com.storix.domain.domains.works.domain.WorksType;

import java.util.List;
import java.util.Set;

public record AiServerSearchPolicy(
        boolean adultDefault,
        boolean adultVerified,
        Set<WorksType> workTypes,
        List<Long> excludedWorkIds
) {
}
