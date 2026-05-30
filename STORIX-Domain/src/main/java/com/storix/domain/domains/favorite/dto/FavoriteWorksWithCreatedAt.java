package com.storix.domain.domains.favorite.dto;

import java.time.LocalDateTime;

public record FavoriteWorksWithCreatedAt(
        Long worksId,
        LocalDateTime createdAt
) {
}
