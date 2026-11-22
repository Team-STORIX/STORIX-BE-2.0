package com.storix.infrastructure.external.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] AI 서버에 넘기는 대화 맥락")
class AiServerChatContextTest {

    private static AiChatMessageInfo message(long id, AiChatMessageRole role, String content) {
        return new AiChatMessageInfo(id, role, AiChatMessageType.TEXT, content,
                role == AiChatMessageRole.USER ? null : AiChatAnswerStatus.OK, null, null);
    }

    @Test
    @DisplayName("CHATBOT 은 assistant 로, 보여 준 작품은 겹치지 않게 순서대로 모은다")
    void buildContext() {
        List<AiChatMessageInfo> messages = List.of(
                message(1L, AiChatMessageRole.USER, "로판 추천"),
                message(2L, AiChatMessageRole.CHATBOT, "이거 어때요"),
                message(3L, AiChatMessageRole.USER, "다른 거"),
                message(4L, AiChatMessageRole.CHATBOT, "이건요"));
        Map<Long, List<Long>> workIds = Map.of(
                2L, List.of(10L, 11L),
                4L, List.of(11L, 12L));

        AiServerChatContext context = AiServerChatContext.of(messages, workIds);

        assertThat(context.turns()).extracting(AiServerChatTurn::role)
                .containsExactly("user", "assistant", "user", "assistant");
        assertThat(context.turns().get(1).workIds()).containsExactly(10L, 11L);
        assertThat(context.turns().get(0).workIds()).isEmpty();
        assertThat(context.shownWorkIds()).containsExactly(10L, 11L, 12L);
        assertThat(context.conditions()).isEmpty();
    }
}
