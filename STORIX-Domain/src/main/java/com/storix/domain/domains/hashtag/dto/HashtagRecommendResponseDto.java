package com.storix.domain.domains.hashtag.dto;

public record HashtagRecommendResponseDto(
        Long id,
        String name,
        Long score
) {
}
