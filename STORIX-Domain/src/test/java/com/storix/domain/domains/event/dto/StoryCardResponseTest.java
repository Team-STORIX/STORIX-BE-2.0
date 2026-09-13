package com.storix.domain.domains.event.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[오늘의 스토리 카드] 카드 이미지 URL 변환")
class StoryCardResponseTest {

    private static final String BASE_URL = "https://cdn.storix.kr";

    private static StoryCardResponse card() {
        return StoryCardResponse.builder()
                .aiImageUrl("public/event/story-card/ai/thriller.png")
                .backgroundImageUrl("public/event/story-card/background/thriller.png")
                .iconImageUrl("public/event/story-card/icon/thriller.png")
                .build();
    }

    @Nested
    @DisplayName("withBaseUrl - 이미지 버전 부여")
    class WithBaseUrl {

        @Test
        @DisplayName("버전이 있으면 세 이미지 URL 모두에 ?v= 가 붙는다")
        void appendsVersionToEveryImageUrl() {
            StoryCardResponse result = card().withBaseUrl(BASE_URL, "20260913");

            assertThat(result.aiImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/ai/thriller.png?v=20260913");
            assertThat(result.backgroundImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/background/thriller.png?v=20260913");
            assertThat(result.iconImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/icon/thriller.png?v=20260913");
        }

        @Test
        @DisplayName("버전이 비어 있으면 기존처럼 쿼리 없이 전체 URL만 만든다")
        void omitsVersionWhenBlank() {
            assertThat(card().withBaseUrl(BASE_URL, null).aiImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/ai/thriller.png");
            assertThat(card().withBaseUrl(BASE_URL, "  ").aiImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/ai/thriller.png");
        }

        @Test
        @DisplayName("objectKey가 비어 있으면 baseUrl을 붙이지 않고 그대로 둔다")
        void keepsBlankObjectKeyAsIs() {
            StoryCardResponse blank = StoryCardResponse.builder()
                    .aiImageUrl(null)
                    .backgroundImageUrl("")
                    .build();

            StoryCardResponse result = blank.withBaseUrl(BASE_URL, "20260913");

            assertThat(result.aiImageUrl()).isNull();
            assertThat(result.backgroundImageUrl()).isEmpty();
        }

        @Test
        @DisplayName("objectKey에 이미 쿼리가 있으면 & 로 이어 붙인다")
        void usesAmpersandWhenQueryAlreadyExists() {
            StoryCardResponse withQuery = StoryCardResponse.builder()
                    .aiImageUrl("public/event/story-card/ai/thriller.png?raw=1")
                    .build();

            assertThat(withQuery.withBaseUrl(BASE_URL, "20260913").aiImageUrl())
                    .isEqualTo("https://cdn.storix.kr/public/event/story-card/ai/thriller.png?raw=1&v=20260913");
        }

        @Test
        @DisplayName("이미지 외 필드는 그대로 유지된다")
        void preservesOtherFields() {
            StoryCardResponse result = card().toBuilder()
                    .genre("스릴러")
                    .immersion("몰입력")
                    .build()
                    .withBaseUrl(BASE_URL, "20260913");

            assertThat(result.genre()).isEqualTo("스릴러");
            assertThat(result.immersion()).isEqualTo("몰입력");
        }
    }
}
