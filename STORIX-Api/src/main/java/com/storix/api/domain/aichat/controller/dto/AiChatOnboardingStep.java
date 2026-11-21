package com.storix.api.domain.aichat.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatOnboardingStep(
        @Schema(description = "GREETING · ADULT · WORKS_TYPE · DONE", example = "WORKS_TYPE")
        String step,
        @Schema(description = "챗봇 말풍선 문구")
        String message,
        @Schema(description = "선택지. 비어 있으면 말풍선만 보여 준다")
        List<AiChatOnboardingOption> options,
        @Schema(description = "여러 개 선택 가능 여부")
        boolean multiSelect
) {
    public static AiChatOnboardingStep message(String step, String message) {
        return new AiChatOnboardingStep(step, message, List.of(), false);
    }

    public static AiChatOnboardingStep singleSelect(String step, String message, List<AiChatOnboardingOption> options) {
        return new AiChatOnboardingStep(step, message, options, false);
    }

    public static AiChatOnboardingStep multiSelect(String step, String message, List<AiChatOnboardingOption> options) {
        return new AiChatOnboardingStep(step, message, options, true);
    }
}
