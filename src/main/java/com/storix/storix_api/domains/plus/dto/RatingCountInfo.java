package com.storix.storix_api.domains.plus.dto;

import com.storix.storix_api.domains.plus.domain.Rating;

public record RatingCountInfo(
        Rating rating,
        Long count
) {
}
