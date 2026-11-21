package com.storix.api.domain.aichat.controller.dto;

import com.storix.api.domain.aichat.stream.AiChatRawWork;
import com.storix.domain.domains.aichat.dto.AiChatMessagePage;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record AiChatMessagesResponse(
        @Schema(description = "오래된 순. 화면 위에서 아래로 그대로 그리면 된다")
        List<AiChatMessageResponse> messages,
        @Schema(description = "더 이전 대화가 있는지. true 면 messages 맨 앞 messageId 를 before 로 다시 부른다")
        boolean hasNext
) {
    public static AiChatMessagesResponse of(AiChatMessagePage page, Map<Long, List<AiChatRawWork>> worksByMessage,
                                            Map<Long, WorksCardInfo> cards) {
        List<AiChatMessageResponse> messages = page.messages().stream()
                .map(message -> AiChatMessageResponse.of(message,
                        worksByMessage.getOrDefault(message.messageId(), List.of()), cards))
                .toList();
        return new AiChatMessagesResponse(messages, page.hasNext());
    }
}
