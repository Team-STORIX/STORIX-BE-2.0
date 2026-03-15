package com.storix.storix_api.domains.plus.dto;

import com.storix.storix_api.domains.plus.domain.Rating;

public record ReviewedWorksIdAndRatingInfo(
        Long worksId,
        Long reviewId,
        Rating rating
) {
}
