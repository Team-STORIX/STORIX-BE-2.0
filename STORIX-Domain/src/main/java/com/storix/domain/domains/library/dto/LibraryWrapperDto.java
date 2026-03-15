package com.storix.domain.domains.library.dto;

import org.springframework.data.domain.Slice;

public record LibraryWrapperDto <T> (
        int totalReviewCount,
        Slice<T> result
) {
}
