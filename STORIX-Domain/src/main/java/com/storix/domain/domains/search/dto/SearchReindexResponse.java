package com.storix.domain.domains.search.dto;

public record SearchReindexResponse(
        String index,
        long indexedCount
) {
}
