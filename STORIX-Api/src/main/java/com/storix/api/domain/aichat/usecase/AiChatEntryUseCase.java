package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatEntryResponse;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.aichat.service.AiChatSettingService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class AiChatEntryUseCase {

    private final AiChatSettingService aiChatSettingService;
    private final AdultVerificationService adultVerificationService;

    public CustomResponse<AiChatEntryResponse> enter(Long userId) {
        AiChatSettingInfo setting = aiChatSettingService.find(userId);
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;

        return CustomResponse.onSuccess(
                SuccessCode.AI_CHAT_ENTRY_LOAD_SUCCESS,
                AiChatEntryResponse.of(setting, adultVerified));
    }
}
