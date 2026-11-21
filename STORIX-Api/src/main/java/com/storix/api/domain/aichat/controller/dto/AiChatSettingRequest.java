package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.works.domain.WorksType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AiChatSettingRequest(
        @NotNull
        @Schema(description = "19금 작품도 추천받기. 성인 인증된 사용자만 true 가능", example = "false")
        Boolean adultEnabled,
        @NotNull
        @Schema(description = "추천받을 작품 유형 (WEBTOON · WEBNOVEL · BOOK). 빈 배열은 상관없음", example = "[\"WEBTOON\"]")
        List<WorksType> worksTypes,
        @Schema(description = "온보딩 마지막 단계에서 저장할 때만 진입 API 의 onboarding.version 을 넣는다. 설정 화면에서는 비운다",
                example = "1", nullable = true)
        Integer onboardingVersion
) {
}
