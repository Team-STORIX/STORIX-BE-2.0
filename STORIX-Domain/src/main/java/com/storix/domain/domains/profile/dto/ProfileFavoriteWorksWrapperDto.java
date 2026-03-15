package com.storix.domain.domains.profile.dto;

import org.springframework.data.domain.Slice;

public record ProfileFavoriteWorksWrapperDto<T> (
        int totalFavoriteWorksCount,
        Slice<T> result
) {
}
