package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.aichat.dto.AiChatMessagePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] 지난 대화 조회")
class AiChatMessageServiceTest {

    @Mock
    private AiChatAdaptor aiChatAdaptor;
    @Mock
    private AiChatMessageAdaptor aiChatMessageAdaptor;
    @InjectMocks
    private AiChatMessageService aiChatMessageService;

    private static AiChatMessage message(long id) {
        AiChatMessage message = AiChatMessage.question(10L, "질문 " + id);
        ReflectionTestUtils.setField(message, "id", id);
        return message;
    }

    private static List<AiChatMessage> newestFirst(long from, long to) {
        return LongStream.iterate(from, id -> id >= to, id -> id - 1).mapToObj(AiChatMessageServiceTest::message).toList();
    }

    @Test
    @DisplayName("최근 size 개를 오래된 순으로 주고, 더 있으면 hasNext")
    void latestPageInChronologicalOrder() {
        AiChat chat = AiChat.of(1L);
        ReflectionTestUtils.setField(chat, "id", 10L);
        given(aiChatAdaptor.findByUserId(1L)).willReturn(Optional.of(chat));
        given(aiChatMessageAdaptor.findLatest(10L, null, 4)).willReturn(newestFirst(9, 6));

        AiChatMessagePage page = aiChatMessageService.findMessages(1L, null, 3);

        assertThat(page.messages()).extracting(AiChatMessageInfo::messageId).containsExactly(7L, 8L, 9L);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("마지막 페이지면 hasNext 가 false")
    void lastPage() {
        AiChat chat = AiChat.of(1L);
        ReflectionTestUtils.setField(chat, "id", 10L);
        given(aiChatAdaptor.findByUserId(1L)).willReturn(Optional.of(chat));
        given(aiChatMessageAdaptor.findLatest(10L, 7L, 4)).willReturn(newestFirst(6, 5));

        AiChatMessagePage page = aiChatMessageService.findMessages(1L, 7L, 3);

        assertThat(page.messages()).extracting(AiChatMessageInfo::messageId).containsExactly(5L, 6L);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("대화방이 없으면 빈 목록")
    void noChat() {
        given(aiChatAdaptor.findByUserId(1L)).willReturn(Optional.empty());

        AiChatMessagePage page = aiChatMessageService.findMessages(1L, null, 20);

        assertThat(page.messages()).isEmpty();
        assertThat(page.hasNext()).isFalse();
    }
}
