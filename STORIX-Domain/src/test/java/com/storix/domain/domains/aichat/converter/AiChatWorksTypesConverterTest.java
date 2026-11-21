package com.storix.domain.domains.aichat.converter;

import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] 작품 유형 목록 저장 형식")
class AiChatWorksTypesConverterTest {

    private final AiChatWorksTypesConverter converter = new AiChatWorksTypesConverter();

    @Test
    @DisplayName("enum 선언 순서로 쉼표로 이어 저장한다")
    void toDatabase() {
        assertThat(converter.convertToDatabaseColumn(Set.of(WorksType.BOOK, WorksType.WEBTOON)))
                .isEqualTo("WEBTOON,BOOK");
    }

    @Test
    @DisplayName("비어 있으면 빈 문자열, 읽을 때도 빈 목록")
    void emptyMeansAny() {
        assertThat(converter.convertToDatabaseColumn(Set.of())).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }

    @Test
    @DisplayName("저장한 값을 그대로 읽는다")
    void roundTrip() {
        Set<WorksType> types = Set.of(WorksType.WEBNOVEL, WorksType.BOOK);

        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(types)))
                .containsExactlyInAnyOrderElementsOf(types);
    }
}
