package com.storix.domain.domains.works.dto;

import java.util.List;
import java.util.Map;

public record WorksMergeResult(
        Long keepWorksId,
        List<Long> mergedWorksIds,
        Map<String, Integer> moved,
        Map<String, Integer> removedDuplicates,
        int reviewsCount,
        double avgRating,
        boolean onboardingPromoted
) {
}
