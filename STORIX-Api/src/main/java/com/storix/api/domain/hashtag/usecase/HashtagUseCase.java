package com.storix.api.domain.hashtag.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendationContext;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.hashtag.service.HashtagRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class HashtagUseCase {

    private static final int RECOMMENDATION_LIMIT = 10; // 추천 개수 제한

    private final HashtagRecommendService hashtagRecommendService;

    @Transactional(readOnly = true)
    public List<HashtagRecommendResponseDto> getHashtagRecommendation(Long userId) {

        // 비로그인 사용자 : 전체 인기 해시태그를 추천
        if (userId == null) {
            return hashtagRecommendService.getGlobalPopularHashtags(RECOMMENDATION_LIMIT);
        }

        // 사용자 데이터 수집
        HashtagRecommendationContext context = hashtagRecommendService.collectRecommendationContext(userId);

        // 데이터 기반 개인화 추천 해시 태그 계산
        List<HashtagRecommendResponseDto> personalized =
                hashtagRecommendService.calculatePersonalizedHashtags(context, RECOMMENDATION_LIMIT);

        if (personalized != null && personalized.size() >= RECOMMENDATION_LIMIT) {
            return personalized;
        }

        // 추천 결과가 충분하지 않은 경우, 선호 장르 기반으로 추가 추천
        return hashtagRecommendService.fillWithFallback(
                personalized,
                context.favoriteGenres(),
                RECOMMENDATION_LIMIT
        );
    }

}
