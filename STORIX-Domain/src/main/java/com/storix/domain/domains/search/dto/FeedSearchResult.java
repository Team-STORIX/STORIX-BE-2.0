package com.storix.domain.domains.search.dto;

import java.util.List;

public record FeedSearchResult(
        List<Long> boardIds,
        boolean hasNext
) {
}
