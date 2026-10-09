package com.storix.domain.domains.works.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[작품] 적재 시 같은 작품 판정용 값")
class WorksIdentityTest {

    @Test
    @DisplayName("띄어쓰기 · 라벨이 달라도 같은 제목으로 본다")
    void sameTitleKeyIgnoringSpacesAndLabels() {
        assertThat(WorksIdentity.titleKey("감옥 게임")).isEqualTo(WorksIdentity.titleKey("감옥게임"));
        assertThat(WorksIdentity.titleKey("[BL] 충견")).isEqualTo(WorksIdentity.titleKey("충견"));
        assertThat(WorksIdentity.titleKey("서브 남주가 파업하면 생기는 일 [연재]"))
                .isEqualTo(WorksIdentity.titleKey("서브 남주가 파업하면 생기는 일"));
    }

    @Test
    @DisplayName("외전은 본편과 같은 작품으로 본다")
    void sideStoryIsSameWorks() {
        String original = WorksIdentity.titleKey("나 혼자만 레벨업");

        assertThat(WorksIdentity.titleKey("나 혼자만 레벨업 외전")).isEqualTo(original);
        assertThat(WorksIdentity.titleKey("나 혼자만 레벨업 [외전]")).isEqualTo(original);
        assertThat(WorksIdentity.titleKey("외전")).isEqualTo("외전");
    }

    @Test
    @DisplayName("판본 · 편 표기는 다른 작품이라 남긴다")
    void editionsStayDifferent() {
        assertThat(WorksIdentity.titleKey("천관사복 (개정판)")).isNotEqualTo(WorksIdentity.titleKey("천관사복"));
        assertThat(WorksIdentity.titleKey("천관사복 [19세 완전판]")).isNotEqualTo(WorksIdentity.titleKey("천관사복"));
        assertThat(WorksIdentity.titleKey("전지적 독자 시점 2")).isNotEqualTo(WorksIdentity.titleKey("전지적 독자 시점"));
        assertThat(WorksIdentity.titleKey("마도조사 [단행본]")).isNotEqualTo(WorksIdentity.titleKey("마도조사"));
    }

    @Test
    @DisplayName("단행본은 단행본 표기를 뺀 제목으로 웹소설과 비교한다")
    void bookEdition() {
        assertThat(WorksIdentity.isBookEdition("마도조사 [단행본]", WorksType.WEBNOVEL)).isTrue();
        assertThat(WorksIdentity.isBookEdition("마도조사", WorksType.BOOK)).isTrue();
        assertThat(WorksIdentity.isBookEdition("마도조사", WorksType.WEBNOVEL)).isFalse();
        assertThat(WorksIdentity.bookBaseKey("마도조사 [단행본]")).isEqualTo(WorksIdentity.titleKey("마도조사"));
    }

    @Test
    @DisplayName("작가는 구분자로 쪼개 이름 하나하나로 비교한다")
    void artistNames() {
        assertThat(WorksIdentity.artistNames("묵향동후/진강문학성, 백몽사, STARember"))
                .containsExactly("묵향동후", "진강문학성", "백몽사", "starember");
        assertThat(WorksIdentity.artistNames("광풍취고당, 묵향동후"))
                .containsExactlyInAnyOrderElementsOf(WorksIdentity.artistNames("묵향동후, 광풍취고당"));
        assertThat(WorksIdentity.artistNames("추공(원작) & 장성락 · REDICE")).containsExactly("추공", "장성락", "redice");
        assertThat(WorksIdentity.artistNames("싱숑 ∙ 글 / 슬리피-C ∙ 그림")).containsExactly("싱숑", "슬리피c");
    }

    @Test
    @DisplayName("작가가 한 명이라도 겹치면 같은 작품 후보다")
    void sharesArtist() {
        Works works = Works.builder()
                .worksName("천관사복")
                .artistName("묵향동후, 백몽사, STARember")
                .build();

        assertThat(WorksIdentity.sharesArtist(WorksIdentity.artistNames("묵향동후/진강문학성, 백몽사"), works)).isTrue();
        assertThat(WorksIdentity.sharesArtist(WorksIdentity.artistNames("다른 작가"), works)).isFalse();
    }

    @Test
    @DisplayName("겹쳐 들어온 작가명은 처음 나온 순서대로 하나씩만 남긴다")
    void dedupeArtistName() {
        assertThat(WorksIdentity.dedupeArtistName("구름고래비누, 구름고래비누, 희서, 희서")).isEqualTo("구름고래비누, 희서");
    }

    @Test
    @DisplayName("작품을 만들 때 비교용 제목이 채워진다")
    void normalizedNameOnBuild() {
        Works works = Works.builder().worksName("[BL] 충견").artistName("작가").build();

        assertThat(works.getNormalizedName()).isEqualTo("충견");
    }
}
