package com.storix.api.domain.hashtag.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendationContext;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.hashtag.service.HashtagCacheHelper;
import com.storix.domain.domains.hashtag.service.HashtagRecommendService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@UseCase
@RequiredArgsConstructor
public class HashtagUseCase {

    private static final int RECOMMENDATION_LIMIT = 15; // 추천 개수 제한

    private final HashtagRecommendService hashtagRecommendService;
    private final HashtagCacheHelper hashtagCacheHelper;

    public List<HashtagRecommendResponseDto> getHashtagRecommendation(Long userId) {

        // 사용자 데이터 수집
        Long totalWorksCount = hashtagCacheHelper.findTotalWorksCount();
        if (totalWorksCount == null) {
            totalWorksCount = hashtagRecommendService.countAllWorks();
            hashtagCacheHelper.saveTotalWorksCount(totalWorksCount);
        }
        HashtagRecommendationContext context = hashtagRecommendService.collectRecommendationContext(userId, totalWorksCount);

        // 데이터 기반 개인화 추천 해시 태그 계산
        List<HashtagRecommendResponseDto> personalized =
                hashtagRecommendService.calculatePersonalizedHashtags(context, RECOMMENDATION_LIMIT);

        // 결과 반환
        if (personalized.size() >= RECOMMENDATION_LIMIT) {
            return personalized;
        }

        // 모자라면 선호 장르 · 전체 인기 순위로 채운다. 순위는 캐시에 없을 때만 DB 에서
        List<HashtagRecommendResponseDto> genrePopular = List.of();
        if (!context.favoriteGenres().isEmpty()) {
            genrePopular = hashtagCacheHelper.findPopular(context.favoriteGenres());
            if (genrePopular == null) {
                genrePopular = hashtagRecommendService.loadPopularHashtags(context.favoriteGenres(), RECOMMENDATION_LIMIT);
                hashtagCacheHelper.savePopular(context.favoriteGenres(), genrePopular);
            }
        }

        List<HashtagRecommendResponseDto> globalPopular = hashtagCacheHelper.findPopular(Set.of());
        if (globalPopular == null) {
            globalPopular = hashtagRecommendService.loadPopularHashtags(Set.of(), RECOMMENDATION_LIMIT);
            hashtagCacheHelper.savePopular(Set.of(), globalPopular);
        }

        return hashtagRecommendService.fillWithFallback(
                personalized,
                genrePopular,
                globalPopular,
                RECOMMENDATION_LIMIT
        );
    }

}
