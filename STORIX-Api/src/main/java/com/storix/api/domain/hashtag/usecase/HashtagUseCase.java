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
        HashtagRecommendationContext context = hashtagRecommendService.collectRecommendationContext(userId);

        // 데이터 기반 개인화 추천 해시 태그 계산
        List<HashtagRecommendResponseDto> personalized =
                hashtagRecommendService.calculatePersonalizedHashtags(context, RECOMMENDATION_LIMIT);

        // 결과 반환
        if (personalized.size() >= RECOMMENDATION_LIMIT) {
            return personalized;
        }

        // 추천 결과가 충분하지 않은 경우, 선호 장르 · 전체 인기 순위로 보충. 순위는 캐시에서, 없을 때만 DB 조회
        List<HashtagRecommendResponseDto> genrePopular = context.favoriteGenres().isEmpty()
                ? List.of()
                : hashtagCacheHelper.getOrLoadPopular(context.favoriteGenres(),
                        () -> hashtagRecommendService.loadPopularHashtags(context.favoriteGenres(), RECOMMENDATION_LIMIT));
        List<HashtagRecommendResponseDto> globalPopular = hashtagCacheHelper.getOrLoadPopular(Set.of(),
                () -> hashtagRecommendService.loadPopularHashtags(Set.of(), RECOMMENDATION_LIMIT));

        return hashtagRecommendService.fillWithFallback(
                personalized,
                genrePopular,
                globalPopular,
                RECOMMENDATION_LIMIT
        );
    }

}
