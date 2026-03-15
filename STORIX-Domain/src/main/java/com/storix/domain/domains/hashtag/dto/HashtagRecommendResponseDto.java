package com.storix.domain.domains.hashtag.dto;

public record HashtagRecommendResponseDto(
        Long id,
        String name,
        Long count    // 해당 해시태그가 몇 개의 작품에 쓰였는지
) {
}
