package com.storix.internal.aichat.controller.dto;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse.EnumItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 내부] 작품 enum 카탈로그")
class AiWorksCatalogResponseTest {

    @Test
    @DisplayName("크롤러 카탈로그와 같은 enum 이름 · DB 값 · 저장 형태를 내려준다")
    void sameAsCrawlerCatalog() {
        AiWorksCatalogResponse response = AiWorksCatalogResponse.create();
        WorksEnumCatalogResponse crawler = WorksEnumCatalogResponse.create();

        assertThat(response.genre()).isEqualTo(crawler.genre());
        assertThat(response.ageClassification()).isEqualTo(crawler.ageClassification());
        assertThat(response.worksType()).isEqualTo(crawler.worksType());
        assertThat(response.platform()).isEqualTo(crawler.platform());
        assertThat(response.genre().values()).hasSize(Genre.values().length)
                .contains(new EnumItem("ROFAN", "로판"));
    }

    @Test
    @DisplayName("19금 연령가는 AGE_18 만")
    void adultAgeClassifications() {
        assertThat(AiWorksCatalogResponse.create().adultAgeClassifications()).containsExactly("AGE_18");
    }

    @Test
    @DisplayName("챗봇 설정 작품 유형은 웹툰 · 웹소설 · 단행본. 만화는 빠진다")
    void selectableWorksTypes() {
        assertThat(AiWorksCatalogResponse.create().selectableWorksTypes())
                .containsExactlyInAnyOrder("WEBTOON", "WEBNOVEL", "BOOK")
                .doesNotContain("COMIC");
    }
}
