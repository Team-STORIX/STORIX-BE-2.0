package com.storix.domain.domains.search.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[검색] 한글 정규화 · 초성 · 자모")
class HangulTextHelperTest {

    @Test
    @DisplayName("띄어쓰기와 특수문자를 지우고 영문은 소문자로 바꾼다")
    void normalize() {
        assertThat(HangulTextHelper.normalize("나 혼자만 레벨업: Ragnarok!")).isEqualTo("나혼자만레벨업ragnarok");
        assertThat(HangulTextHelper.normalize(null)).isEmpty();
    }

    @Test
    @DisplayName("본제목은 앞 라벨을 떼고 ':' · '(' · '[' 앞까지만 남긴다")
    void baseTitle() {
        assertThat(HangulTextHelper.baseTitle("패션(PASSION)")).isEqualTo("패션");
        assertThat(HangulTextHelper.baseTitle("패션 : 라가(raga)")).isEqualTo("패션");
        assertThat(HangulTextHelper.baseTitle("나 혼자만 레벨업 : 라그나로크")).isEqualTo("나혼자만레벨업");
        assertThat(HangulTextHelper.baseTitle("[개정판] [19세 완전판] 패션 [외전]")).isEqualTo("패션");
        assertThat(HangulTextHelper.baseTitle("패션왕")).isEqualTo("패션왕");
        assertThat(HangulTextHelper.baseTitle("(개정판)")).isEqualTo("개정판");
        assertThat(HangulTextHelper.baseTitle(null)).isEmpty();
    }

    @Test
    @DisplayName("음절은 초성으로, 나머지 글자는 그대로 둔다")
    void chosung() {
        assertThat(HangulTextHelper.chosung("나 혼자만 레벨업 2")).isEqualTo("ㄴㅎㅈㅁㄹㅂㅇ2");
    }

    @Test
    @DisplayName("음절을 초성 · 중성 · 종성으로 나눈다")
    void jamo() {
        assertThat(HangulTextHelper.jamo("레벨업")).isEqualTo("ㄹㅔㅂㅔㄹㅇㅓㅂ");
        assertThat(HangulTextHelper.jamo("닭a")).isEqualTo("ㄷㅏㄺa");
    }

    @Test
    @DisplayName("자음으로만 된 검색어만 초성 검색으로 본다")
    void isChosungOnly() {
        assertThat(HangulTextHelper.isChosungOnly("ㄴㅎㅈ ㅁ")).isTrue();
        assertThat(HangulTextHelper.isChosungOnly("ㄴ혼ㅈ")).isFalse();
        assertThat(HangulTextHelper.isChosungOnly("ㅏㅓ")).isFalse();
        assertThat(HangulTextHelper.isChosungOnly(" ")).isFalse();
    }
}
