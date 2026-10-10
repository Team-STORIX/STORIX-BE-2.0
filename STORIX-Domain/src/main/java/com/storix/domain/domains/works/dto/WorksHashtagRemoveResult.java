package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksHashtagRemoveResult(
        Long worksId,
        List<String> removed,
        List<String> notFound
) {
    public static WorksHashtagRemoveResult of(Long worksId, List<String> names, List<String> removed) {
        return new WorksHashtagRemoveResult(
                worksId,
                removed,
                names.stream().filter(name -> !removed.contains(name)).toList()
        );
    }
}
