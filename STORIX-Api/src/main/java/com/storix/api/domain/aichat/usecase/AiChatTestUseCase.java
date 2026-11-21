package com.storix.api.domain.aichat.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.aichat.service.AiChatResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@UseCase
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class AiChatTestUseCase {

    private final AiChatResetService aiChatResetService;

    public CustomResponse<Void> reset(Long userId) {
        aiChatResetService.reset(userId);

        return CustomResponse.onSuccess(SuccessCode.AI_CHAT_RESET_SUCCESS);
    }
}
