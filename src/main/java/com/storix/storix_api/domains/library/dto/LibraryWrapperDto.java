package com.storix.storix_api.domains.library.dto;

import org.springframework.data.domain.Slice;

public record LibraryWrapperDto <T> (
        int totalReviewCount,
        Slice<T> result
) {
}
