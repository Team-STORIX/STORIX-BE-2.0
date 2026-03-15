package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.Rating;

public record ReviewedWorksIdAndRatingInfo(
        Long worksId,
        Long reviewId,
        Rating rating
) {
}
