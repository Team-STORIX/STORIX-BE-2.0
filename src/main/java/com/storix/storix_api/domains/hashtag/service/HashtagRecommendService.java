package com.storix.storix_api.domains.hashtag.service;

import com.storix.storix_api.domains.hashtag.application.port.LoadHashtagPort;
import com.storix.storix_api.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.storix_api.domains.user.application.port.LoadUserPort;
import com.storix.storix_api.domains.user.domain.User;
import com.storix.storix_api.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagRecommendService {

    private final LoadUserPort loadUserPort;
    private final LoadHashtagPort loadHashtagPort;

    private static final int RECOMMENDATION_LIMIT = 10; // 추천 개수 제한

    public List<HashtagRecommendResponseDto> getRecommendedHashtags(Long userId) {
        // 1. 비로그인 유저(Guest) -> 전체 인기 태그 추천
        if (userId == null) {
            return loadHashtagPort.recommendGlobalPopular(RECOMMENDATION_LIMIT);
        }

        // 2. 로그인 유저 정보 로드
        User user = loadUserPort.findById(userId);
        Set<Genre> favoriteGenres = user.getFavoriteGenreList();

        // 3. 선호 장르가 설정되지 않은 유저 -> 전체 인기 태그 추천
        if (favoriteGenres == null || favoriteGenres.isEmpty()) {
            return loadHashtagPort.recommendGlobalPopular(RECOMMENDATION_LIMIT);
        }

        // 4. 선호 장르 기반 추천
        List<HashtagRecommendResponseDto> recommendations =
                loadHashtagPort.recommendByGenres(favoriteGenres, RECOMMENDATION_LIMIT);

        if (recommendations.isEmpty()) {
            return loadHashtagPort.recommendGlobalPopular(RECOMMENDATION_LIMIT);
        }

        return recommendations;
    }
}