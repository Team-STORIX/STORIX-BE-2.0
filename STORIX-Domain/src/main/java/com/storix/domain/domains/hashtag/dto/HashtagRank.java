package com.storix.domain.domains.hashtag.dto;

public record HashtagRank(
        Long id,
        String name,
        double finalScore,
        int positiveCount
) {

    public HashtagRecommendResponseDto toResponse() {
        return new HashtagRecommendResponseDto(id, name, Math.round(finalScore));
    }
}
