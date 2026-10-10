package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[작품] 상세 작가 표기")
class WorksDetailResponseDtoTest {

    private WorksDetailResponseDto detail(String originalAuthor, String author, String illustrator) {
        return detail(WorksType.WEBTOON, originalAuthor, author, illustrator);
    }

    private WorksDetailResponseDto detail(WorksType worksType, String originalAuthor, String author, String illustrator) {
        Works works = Works.builder().worksName("작품").artistName("작가").worksType(worksType)
                .originalAuthor(originalAuthor).author(author).illustrator(illustrator).build();
        return WorksDetailResponseDto.from(works, 0L, false);
    }

    @Test
    @DisplayName("원작 작가를 글 작가 앞에 붙인다")
    void originalAuthorFirst() {
        WorksDetailResponseDto dto = detail("원작가", "각색가", "그림가");

        assertThat(dto.author()).isEqualTo("원작가, 각색가");
        assertThat(dto.illustrator()).isEqualTo("그림가");
        assertThat(dto.originalAuthor()).isEqualTo("원작가");
    }

    @Test
    @DisplayName("앞에 나온 이름은 빼서 같은 이름이 두 번 나오지 않는다")
    void dedupe() {
        WorksDetailResponseDto dto = detail("글비", "글비, 글비", "글비, 그림가");

        assertThat(dto.author()).isEqualTo("글비");
        assertThat(dto.illustrator()).isEqualTo("그림가");
    }

    @Test
    @DisplayName("그림 작가가 글 작가와 같으면 그림 작가는 비운다")
    void sameIllustrator() {
        WorksDetailResponseDto dto = detail(null, "추공", "추공");

        assertThat(dto.author()).isEqualTo("추공");
        assertThat(dto.illustrator()).isNull();
    }

    @Test
    @DisplayName("웹소설 · 단행본은 표지 일러스트레이터를 그림 작가로 내려주지 않는다")
    void novelHidesIllustrator() {
        assertThat(detail(WorksType.WEBNOVEL, "묵향동후", "묵향동후", "千二百").illustrator()).isNull();
        assertThat(detail(WorksType.BOOK, null, "작가", "표지가").illustrator()).isNull();
        assertThat(detail(WorksType.COMIC, null, "작가", "그림가").illustrator()).isEqualTo("그림가");
    }

    private WorksDetailResponseDto detailWithHashtags(Genre genre, String... names) {
        Works works = Works.builder().worksName("작품").artistName("작가").worksType(WorksType.WEBTOON).genre(genre).build();
        Set<Hashtag> hashtags = Stream.of(names).map(Hashtag::new).collect(Collectors.toSet());
        works.addHashtags(hashtags);
        return WorksDetailResponseDto.from(works, 0L, false);
    }

    @Test
    @DisplayName("BL 키워드는 공 → 수 → 나머지 순, 같은 묶음은 가나다순")
    void blHashtagOrder() {
        WorksDetailResponseDto dto = detailWithHashtags(Genre.BL, "현대물", "상처수", "집착공", "다정공", "순진수", "오메가버스");

        assertThat(dto.hashtags()).containsExactly("다정공", "집착공", "상처수", "순진수", "오메가버스", "현대물");
    }

    @Test
    @DisplayName("두 글자 이하는 공 · 수로 보지 않는다")
    void blShortHashtagIsNotRole() {
        WorksDetailResponseDto dto = detailWithHashtags(Genre.BL, "복수", "가수", "대공", "다정공");

        assertThat(dto.hashtags()).containsExactly("다정공", "가수", "대공", "복수");
    }
}
