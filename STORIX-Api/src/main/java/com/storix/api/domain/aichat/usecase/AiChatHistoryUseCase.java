package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessagesResponse;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.api.domain.aichat.helper.AiChatWorksJsonHelper;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.domain.domains.aichat.dto.AiChatMessagePage;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import com.storix.domain.domains.works.service.WorksCardService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@UseCase
@RequiredArgsConstructor
public class AiChatHistoryUseCase {

    private final AiChatMessageService aiChatMessageService;
    private final WorksCardService worksCardService;
    private final AiChatWorksJsonHelper aiChatWorksJson;
    private final AdultVerificationService adultVerificationService;

    public CustomResponse<AiChatMessagesResponse> findMessages(Long userId, Long beforeId, int size) {
        AiChatMessagePage page = aiChatMessageService.findMessages(userId, beforeId, size);
        Map<Long, List<AiChatRawWork>> worksByMessage = aiChatWorksJson.readAll(page.messages());
        Map<Long, WorksCardInfo> cards = worksCardService.findCards(AiChatWorksJsonHelper.workIds(worksByMessage.values()));
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;

        return CustomResponse.onSuccess(SuccessCode.AI_CHAT_MESSAGES_LOAD_SUCCESS,
                AiChatMessagesResponse.of(page, worksByMessage, cards, adultVerified));
    }
}
