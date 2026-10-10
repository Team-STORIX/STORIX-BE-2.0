package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
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

        List<String> changes = works.updateFromImport(null, "장성락", null, AgeClassification.AGE_18, null, null, null, "https://new.png");

        assertThat(changes).containsExactly("illustrator:null->장성락", "ageClassification:AGE_15->AGE_18", "thumbnailUrl:https://old.png->https://new.png");
        assertThat(works.getIllustrator()).isEqualTo("장성락");
        assertThat(works.getAgeClassification()).isEqualTo(AgeClassification.AGE_18);
        assertThat(works.getThumbnailUrl()).isEqualTo("https://new.png");
    }

    @Test
    @DisplayName("새 값이 비어 있으면 기존 값을 둔다")
    void keepWhenBlank() {
        Works works = works();

        List<String> changes = works.updateFromImport(" ", null, "", null, null, null, null, null);

        assertThat(changes).isEmpty();
        assertThat(works.getAuthor()).isEqualTo("추공");
        assertThat(works.getGenre()).isEqualTo(Genre.FANTASY);
        assertThat(works.getDescription()).isEqualTo("기존 소개");
    }

    @Test
    @DisplayName("같은 값이 오면 바뀌지 않았다고 알린다")
    void unchangedWhenSame() {
        Works works = works();

        assertThat(works.updateFromImport("추공", null, null, AgeClassification.AGE_15, Genre.FANTASY, WorksType.WEBTOON, "기존 소개", "https://old.png"))
                .isEmpty();
    }

    @Test
    @DisplayName("플랫폼은 없으면 추가하고, 있으면 링크만 갱신한다")
    void putPlatform() {
        Works works = works();

        assertThat(works.putPlatform(Platform.KAKAO_PAGE, null)).isEqualTo("platform:+KAKAO_PAGE");
        assertThat(works.putPlatform(Platform.KAKAO_PAGE, null)).isNull();
        assertThat(works.putPlatform(Platform.KAKAO_PAGE, "https://page.kakao.com/1")).isEqualTo("landingUrl:KAKAO_PAGE:null->https://page.kakao.com/1");
        assertThat(works.getPlatforms()).hasSize(1);
        assertThat(works.getPlatforms().iterator().next().getLandingUrl()).isEqualTo("https://page.kakao.com/1");
    }

    @Test
    @DisplayName("해시태그는 기존에 더하기만 하고 빼지 않는다")
    void addHashtags() {
        Works works = works();
        Hashtag hunter = new Hashtag("헌터");
        Hashtag fantasy = new Hashtag("판타지");

        assertThat(works.addHashtags(Set.of(hunter))).isEqualTo("hashtags:+헌터");
        assertThat(works.addHashtags(Set.of(new Hashtag("헌터")))).isNull();
        assertThat(works.addHashtags(Set.of(fantasy, hunter))).isEqualTo("hashtags:+판타지");
        assertThat(works.addHashtags(Set.of())).isNull();
        assertThat(works.getHashtags()).extracting(Hashtag::getName).containsExactlyInAnyOrder("헌터", "판타지");
    }

    @Test
    @DisplayName("새로 만든 작품은 온보딩 작품이 아니다")
    void newWorksIsNotOnboarding() {
        assertThat(works().getIsOnboarding()).isFalse();
    }

    @Test
    @DisplayName("연령은 기존보다 낮으면 바꾸지 않는다")
    void ageNeverLowered() {
        Works works = works();

        List<String> changes = works.updateFromImport(null, null, null, AgeClassification.ALL, null, null, null, null);

        assertThat(changes).isEmpty();
        assertThat(works.getAgeClassification()).isEqualTo(AgeClassification.AGE_15);
    }

    @Test
    @DisplayName("병합하면 keep 과 drop 중 높은 연령이 남는다")
    void mergeKeepsHigherAge() {
        Works keep = Works.builder().worksName("유언 때문에 죽는 건 잠깐 미뤘습니다").artistName("소림")
                .ageClassification(AgeClassification.ALL).build();
        Works drop = Works.builder().worksName("유언 때문에 죽는 건 잠깐 미뤘습니다 2~5권").artistName("소림")
                .ageClassification(AgeClassification.AGE_15).build();

        keep.mergeFrom(drop);

        assertThat(keep.getAgeClassification()).isEqualTo(AgeClassification.AGE_15);
    }

    @Test
    @DisplayName("병합할 때 drop 연령이 낮으면 keep 연령을 둔다")
    void mergeKeepsKeepAgeWhenDropLower() {
        Works keep = works();
        Works drop = Works.builder().worksName("나 혼자만 레벨업").artistName("추공")
                .ageClassification(AgeClassification.ALL).build();

        keep.mergeFrom(drop);

        assertThat(keep.getAgeClassification()).isEqualTo(AgeClassification.AGE_15);
    }
}
