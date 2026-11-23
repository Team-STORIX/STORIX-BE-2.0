package com.storix.internal.aichat.controller.dto;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse.EnumBlock;

import java.util.Arrays;
import java.util.List;

public record AiWorksCatalogResponse(
        EnumBlock genre,
        EnumBlock ageClassification,
        EnumBlock worksType,
        EnumBlock platform,
        List<String> adultAgeClassifications,
        List<String> selectableWorksTypes
) {

    // 크롤러 카탈로그 + AI 전용 규칙 (19금 연령가, 챗봇 설정 작품 유형)
    public static AiWorksCatalogResponse create() {
        WorksEnumCatalogResponse catalog = WorksEnumCatalogResponse.create();
        return new AiWorksCatalogResponse(
                catalog.genre(),
                catalog.ageClassification(),
                catalog.worksType(),
                catalog.platform(),
                Arrays.stream(AgeClassification.values())
                        .filter(AdultContentPolicy::isAdultOnly)
                        .map(Enum::name)
                        .toList(),
                AiChatSetting.selectableWorksTypes().stream()
                        .map(Enum::name)
                        .toList()
        );
    }
}
