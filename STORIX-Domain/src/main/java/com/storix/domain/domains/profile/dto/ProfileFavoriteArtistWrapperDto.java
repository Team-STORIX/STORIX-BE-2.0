package com.storix.domain.domains.profile.dto;

import org.springframework.data.domain.Slice;

public record ProfileFavoriteArtistWrapperDto<T> (
        int totalFavoriteArtistCount,
        Slice<T> result
) {
}
