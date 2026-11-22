package com.storix.internal.aichat.controller.dto;

import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.works.domain.Genre;

import java.util.Collection;
import java.util.List;

// STORIX-AI schemas/user_context.py 의 UserContext 와 같은 모양. 닉네임 · 이메일은 넣지 않는다
public record AiUserContextResponse(
        List<String> preferredGenres,
        List<String> preferredHashtags,
        List<Long> libraryWorkIds,
        List<Long> favoriteWorkIds
) {
    private static final int GENRE_LIMIT = 3;

    // 장르는 활동으로 쌓인 점수 상위. 점수가 없으면 가입 때 고른 장르
    public static AiUserContextResponse of(List<GenreScoreInfo> genreScores, Collection<Genre> signupGenres,
                                           List<HashtagRecommendResponseDto> hashtags,
                                           List<Long> libraryWorkIds, List<Long> favoriteWorkIds) {
        List<String> scored = genreScores.stream()
                .filter(score -> score.score() != null && score.score() > 0)
                .limit(GENRE_LIMIT)
                .map(GenreScoreInfo::genre)
                .toList();
        List<String> genres = scored.isEmpty()
                ? signupGenres.stream().sorted().map(Genre::getDbValue).limit(GENRE_LIMIT).toList()
                : scored;
        return new AiUserContextResponse(genres,
                hashtags.stream().map(HashtagRecommendResponseDto::name).toList(),
                libraryWorkIds, favoriteWorkIds);
    }
}
