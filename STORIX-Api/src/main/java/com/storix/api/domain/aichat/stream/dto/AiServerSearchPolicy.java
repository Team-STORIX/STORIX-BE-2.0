package com.storix.api.domain.aichat.stream.dto;

import com.storix.domain.domains.works.domain.WorksType;

import java.util.List;
import java.util.Set;

public record AiServerSearchPolicy(
        boolean adultAllowed,
        Set<WorksType> workTypes,
        List<Long> excludedWorkIds
) {
}
