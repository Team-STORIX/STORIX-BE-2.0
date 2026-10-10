package com.storix.domain.domains.genrescore.dto;

import java.util.Collections;
import java.util.Set;

public record GenreScoreChunkResult(
        int processedLogs,
        int groups,
        Set<Long> users
) {

    public static GenreScoreChunkResult empty() {
        return new GenreScoreChunkResult(0, 0, Collections.emptySet());
    }
}
