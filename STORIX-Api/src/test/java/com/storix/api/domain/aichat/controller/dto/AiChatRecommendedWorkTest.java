package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] 추천 카드 19금 표지")
class AiChatRecommendedWorkTest {

    private static final AiChatRawWork RAW = new AiChatRawWork(1, 5L, "이유",
            List.of(new AiChatMatchedClue("은발 보라 눈", "COVER"), new AiChatMatchedClue("로판", "GENRE")));

    private static WorksCardInfo card(boolean adult) {
        return new WorksCardInfo(5L, "작품", "cover.png", WorksType.WEBTOON, Genre.ROFAN, List.of(Platform.KAKAO_PAGE), adult);
    }

    @Test
    @DisplayName("성인 인증 전이면 19금 작품 표지와 표지 단서를 가린다")
    void blindAdultCoverWhenNotVerified() {
        AiChatRecommendedWork work = AiChatRecommendedWork.of(RAW, card(true), false);

        assertThat(work.isAdultOnly()).isTrue();
        assertThat(work.isBlinded()).isTrue();
        assertThat(work.thumbnailUrl()).isNull();
        assertThat(work.title()).isEqualTo("작품");
        assertThat(work.matchedClues()).extracting(AiChatMatchedClue::source).containsExactly("GENRE");
    }

    @Test
    @DisplayName("성인 인증이면 19금 작품도 표지를 그대로 보여 준다")
    void showAdultCoverWhenVerified() {
        AiChatRecommendedWork work = AiChatRecommendedWork.of(RAW, card(true), true);

        assertThat(work.isBlinded()).isFalse();
        assertThat(work.thumbnailUrl()).isEqualTo("cover.png");
        assertThat(work.matchedClues()).hasSize(2);
    }

    @Test
    @DisplayName("19금이 아닌 작품은 인증과 상관없이 그대로 보여 준다")
    void showNonAdultCover() {
        AiChatRecommendedWork work = AiChatRecommendedWork.of(RAW, card(false), false);

        assertThat(work.isAdultOnly()).isFalse();
        assertThat(work.isBlinded()).isFalse();
        assertThat(work.thumbnailUrl()).isEqualTo("cover.png");
    }
}
