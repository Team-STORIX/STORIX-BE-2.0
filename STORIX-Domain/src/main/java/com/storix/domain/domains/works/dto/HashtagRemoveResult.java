package com.storix.domain.domains.works.dto;

import java.util.List;
import java.util.Map;

public record HashtagRemoveResult(
        boolean dryRun,
        List<Affected> affected
) {
    public record Affected(
            String name,
            List<Long> worksIds,
            int count
    ) {
    }

    public static HashtagRemoveResult of(boolean dryRun, List<String> names, Map<String, List<Long>> worksIdsByName) {
        return new HashtagRemoveResult(
                dryRun,
                names.stream()
                        .map(name -> {
                            List<Long> worksIds = worksIdsByName.getOrDefault(name, List.of());
                            return new Affected(name, worksIds, worksIds.size());
                        })
                        .toList()
        );
    }
}
