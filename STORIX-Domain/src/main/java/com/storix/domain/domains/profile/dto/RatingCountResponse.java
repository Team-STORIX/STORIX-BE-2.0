package com.storix.domain.domains.profile.dto;


import java.util.Map;

public record RatingCountResponse(
        Map<String, Long> ratingCounts
) {
    public static RatingCountResponse of(
            Map<String, Long> ratingCounts
    ) {
        return new RatingCountResponse(
                ratingCounts
        );
    }
}
