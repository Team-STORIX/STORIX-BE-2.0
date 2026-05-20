package com.storix.domain.domains.hashtag.service;

import com.storix.domain.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;
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
    private final HashtagAdaptor hashtagAdaptor;
    private final UserAdaptor userAdaptor;

    private static final int RECOMMENDATION_LIMIT = 10; // 추천 개수 제한

    public List<HashtagRecommendResponseDto> getGlobalPopularHashtags() {
        return hashtagAdaptor.recommendGlobalPopular(RECOMMENDATION_LIMIT);
    }

    public List<HashtagRecommendResponseDto> getRecommendedHashtags(Long userId) {

        // 1. 로그인 유저 정보 로드
        User user = userAdaptor.findUserById(userId);
        Set<Genre> favoriteGenres = user.getFavoriteGenreList();

        // 2. 선호 장르가 설정되지 않은 유저 -> 전체 인기 태그 추천
        if (favoriteGenres == null || favoriteGenres.isEmpty()) {
            return this.getGlobalPopularHashtags();
        }

        // 3. 선호 장르 기반 추천
        List<HashtagRecommendResponseDto> recommendations =
                hashtagAdaptor.recommendByGenres(favoriteGenres, RECOMMENDATION_LIMIT);

        if (recommendations.isEmpty()) {
            return this.getGlobalPopularHashtags();
        }

        return recommendations;
    }
}