package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.Rating;

public record RatingCountInfo(
        Rating rating,
        Long count
) {
}
