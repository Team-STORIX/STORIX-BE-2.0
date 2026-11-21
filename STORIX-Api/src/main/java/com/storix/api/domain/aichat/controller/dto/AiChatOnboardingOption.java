package com.storix.api.domain.aichat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AiChatOnboardingOption(
        @Schema(description = "설정 저장 때 보낼 값", example = "WEBTOON")
        String value,
        @Schema(description = "버튼 문구", example = "웹툰")
        String label
) {
}
