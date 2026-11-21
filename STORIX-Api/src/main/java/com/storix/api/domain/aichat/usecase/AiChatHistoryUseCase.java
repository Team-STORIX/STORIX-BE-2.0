package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessagesResponse;
import com.storix.api.domain.aichat.stream.AiChatRawWork;
import com.storix.api.domain.aichat.stream.AiChatWorksJson;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
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
    private final AiChatWorksJson aiChatWorksJson;

    // 카드 정보는 저장된 작품 ID 로 지금 작품 DB 에서 채운다
    public CustomResponse<AiChatMessagesResponse> findMessages(Long userId, Long beforeId, int size) {
        AiChatMessagePage page = aiChatMessageService.findMessages(userId, beforeId, size);
        Map<Long, List<AiChatRawWork>> worksByMessage = aiChatWorksJson.readAll(page.messages());
        Map<Long, WorksCardInfo> cards = worksCardService.findCards(AiChatWorksJson.workIds(worksByMessage.values()));

        return CustomResponse.onSuccess(SuccessCode.AI_CHAT_MESSAGES_LOAD_SUCCESS,
                AiChatMessagesResponse.of(page, worksByMessage, cards));
    }
}
