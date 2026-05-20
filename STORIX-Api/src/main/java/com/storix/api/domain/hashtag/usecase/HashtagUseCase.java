package com.storix.api.domain.hashtag.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.hashtag.service.HashtagRecommendService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class HashtagUseCase {

    private final HashtagRecommendService hashtagRecommendService;

    public List<HashtagRecommendResponseDto> getHashtagRecommendation(Long userId) {

        // 1. 비로그인 유저(Guest) -> 전체 인기 태그 추천
        if (userId == null) {
            return hashtagRecommendService.getGlobalPopularHashtags();
        }

        // 2. 로그인 유저 -> 유저 정보 기반 태그 추천
        List<HashtagRecommendResponseDto> recommendations = hashtagRecommendService.getRecommendedHashtags(userId);

        return recommendations;
    }
}
