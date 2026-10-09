package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[작품] 적재 시 기존 작품 갱신 규칙")
class WorksImportMergeTest {

    private Works works() {
        return Works.builder()
                .worksName("나 혼자만 레벨업")
                .artistName("추공")
                .author("추공")
                .ageClassification(AgeClassification.AGE_15)
                .genre(Genre.FANTASY)
                .worksType(WorksType.WEBTOON)
                .description("기존 소개")
                .thumbnailUrl("https://old.png")
                .build();
    }

    @Test
    @DisplayName("새 값이 있으면 덮어쓰고 바뀌었다고 알린다")
    void overwriteWithNewValues() {
        Works works = works();

        boolean changed = works.updateFromImport(null, "장성락", null, AgeClassification.AGE_18, null, null, null, "https://new.png");

        assertThat(changed).isTrue();
        assertThat(works.getIllustrator()).isEqualTo("장성락");
        assertThat(works.getAgeClassification()).isEqualTo(AgeClassification.AGE_18);
        assertThat(works.getThumbnailUrl()).isEqualTo("https://new.png");
    }

    @Test
    @DisplayName("새 값이 비어 있으면 기존 값을 둔다")
    void keepWhenBlank() {
        Works works = works();

        boolean changed = works.updateFromImport(" ", null, "", null, null, null, null, null);

        assertThat(changed).isFalse();
        assertThat(works.getAuthor()).isEqualTo("추공");
        assertThat(works.getGenre()).isEqualTo(Genre.FANTASY);
        assertThat(works.getDescription()).isEqualTo("기존 소개");
    }

    @Test
    @DisplayName("같은 값이 오면 바뀌지 않았다고 알린다")
    void unchangedWhenSame() {
        Works works = works();

        assertThat(works.updateFromImport("추공", null, null, AgeClassification.AGE_15, Genre.FANTASY, WorksType.WEBTOON, "기존 소개", "https://old.png"))
                .isFalse();
    }

    @Test
    @DisplayName("플랫폼은 없으면 추가하고, 있으면 링크만 갱신한다")
    void putPlatform() {
        Works works = works();

        assertThat(works.putPlatform(Platform.KAKAO_PAGE, null)).isTrue();
        assertThat(works.putPlatform(Platform.KAKAO_PAGE, null)).isFalse();
        assertThat(works.putPlatform(Platform.KAKAO_PAGE, "https://page.kakao.com/1")).isTrue();
        assertThat(works.getPlatforms()).hasSize(1);
        assertThat(works.getPlatforms().iterator().next().getLandingUrl()).isEqualTo("https://page.kakao.com/1");
    }

    @Test
    @DisplayName("해시태그는 같은 묶음이면 그대로 두고 다르면 교체한다")
    void replaceHashtags() {
        Works works = works();
        Hashtag hunter = new Hashtag("헌터");

        assertThat(works.replaceHashtags(Set.of(hunter))).isTrue();
        assertThat(works.replaceHashtags(Set.of(hunter))).isFalse();
        assertThat(works.getHashtags()).containsExactly(hunter);
    }

    @Test
    @DisplayName("새로 만든 작품은 온보딩 작품이 아니다")
    void newWorksIsNotOnboarding() {
        assertThat(works().getIsOnboarding()).isFalse();
    }
}
