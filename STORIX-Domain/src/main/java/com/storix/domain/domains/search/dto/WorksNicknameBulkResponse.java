package com.storix.domain.domains.search.dto;

public record WorksNicknameBulkResponse(
        int addedCount,
        int duplicateCount,
        int unknownWorksCount,
        int invalidCount
) {
}
