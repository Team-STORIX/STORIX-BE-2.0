package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatSettingRequest;
import com.storix.api.domain.aichat.controller.dto.AiChatSettingResponse;
import com.storix.api.domain.aichat.onboarding.AiChatOnboardingScript;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.exception.NotAdultVerifiedException;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.aichat.service.AiChatSettingService;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@UseCase
@RequiredArgsConstructor
public class AiChatSettingUseCase {

    private final AiChatSettingService aiChatSettingService;
    private final AdultVerificationService adultVerificationService;

    public CustomResponse<AiChatSettingResponse> find(Long userId) {
        AiChatSettingInfo setting = aiChatSettingService.find(userId);
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;

        return CustomResponse.onSuccess(SuccessCode.AI_CHAT_SETTING_LOAD_SUCCESS,
                AiChatSettingResponse.of(setting, adultVerified));
    }

    public CustomResponse<AiChatSettingResponse> save(Long userId, AiChatSettingRequest request) {
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;
        if (request.adultEnabled() && !adultVerified) {
            throw NotAdultVerifiedException.EXCEPTION;
        }
        if (request.onboardingVersion() != null) {
            AiChatOnboardingScript.requireCurrentVersion(request.onboardingVersion());
        }

        AiChatSettingInfo saved = aiChatSettingService.save(userId, request.adultEnabled(),
                Set.copyOf(request.worksTypes()), request.onboardingVersion(),
                AiChatOnboardingScript.transcript(adultVerified, request.adultEnabled(), request.worksTypes()));

        return CustomResponse.onSuccess(SuccessCode.AI_CHAT_SETTING_SAVE_SUCCESS,
                AiChatSettingResponse.of(saved, adultVerified));
    }
}
