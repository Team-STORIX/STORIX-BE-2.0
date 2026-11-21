package com.storix.api.domain.aichat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatOnboardingResponse(
        @Schema(description = "온보딩 버전. 설정 저장 때 onboardingVersion 으로 그대로 보낸다", example = "1")
        int version,
        @Schema(description = "순서대로 보여 줄 단계")
        List<AiChatOnboardingStep> steps
) {
}
