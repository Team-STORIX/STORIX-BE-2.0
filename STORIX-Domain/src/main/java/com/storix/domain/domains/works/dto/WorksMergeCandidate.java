package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksMergeCandidate(
        Long worksId,
        List<Long> reviewerIds,
        boolean hasTopicRoom
) {
}
