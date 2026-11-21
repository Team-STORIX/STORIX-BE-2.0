package com.storix.internal.aichat.controller.dto;

import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.works.domain.Genre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 내부 API] 사용자 맥락")
class AiUserContextResponseTest {

    @Test
    @DisplayName("장르는 점수가 있는 상위 3개, 해시태그는 이름만")
    void scoredGenres() {
        AiUserContextResponse response = AiUserContextResponse.of(
                List.of(new GenreScoreInfo("로판", 30.0), new GenreScoreInfo("판타지", 20.0),
                        new GenreScoreInfo("BL", 10.0), new GenreScoreInfo("무협", 5.0), new GenreScoreInfo("드라마", 0.0)),
                Set.of(Genre.DRAMA),
                List.of(new HashtagRecommendResponseDto(1L, "회귀", 9L)),
                List.of(10L), List.of(20L, 21L));

        assertThat(response.preferredGenres()).containsExactly("로판", "판타지", "BL");
        assertThat(response.preferredHashtags()).containsExactly("회귀");
        assertThat(response.libraryWorkIds()).containsExactly(10L);
        assertThat(response.favoriteWorkIds()).containsExactly(20L, 21L);
    }

    @Test
    @DisplayName("활동 점수가 없으면 가입 때 고른 장르")
    void fallbackToSignupGenres() {
        AiUserContextResponse response = AiUserContextResponse.of(
                List.of(new GenreScoreInfo("로판", 0.0)),
                Set.of(Genre.FANTASY, Genre.ROMANCE),
                List.of(), List.of(), List.of());

        assertThat(response.preferredGenres()).containsExactly("로맨스", "판타지");
    }
}
