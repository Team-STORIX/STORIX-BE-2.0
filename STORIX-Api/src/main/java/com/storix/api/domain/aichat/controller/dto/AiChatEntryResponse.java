package com.storix.api.domain.aichat.controller.dto;

import com.storix.api.domain.aichat.onboarding.AiChatOnboardingScript;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AiChatEntryResponse(
        @Schema(description = "온보딩을 볼 차례면 내려준다. null 이면 지난 대화를 불러와 바로 이어 간다", nullable = true)
        AiChatOnboardingResponse onboarding,
        @Schema(description = "예시 질문")
        List<String> exampleQuestions
) {
    public static AiChatEntryResponse of(AiChatSettingInfo setting, boolean adultVerified) {
        AiChatOnboardingResponse onboarding = setting.hasSeenOnboarding(AiChatOnboardingScript.VERSION)
                ? null
                : new AiChatOnboardingResponse(AiChatOnboardingScript.VERSION,
                        AiChatOnboardingScript.steps(adultVerified));
        return new AiChatEntryResponse(onboarding, AiChatOnboardingScript.EXAMPLE_QUESTIONS);
    }
}
