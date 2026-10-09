package com.storix.domain.domains.search.dto;

public record WorksReindexResponse(
        String index,
        long indexedCount
) {
}
