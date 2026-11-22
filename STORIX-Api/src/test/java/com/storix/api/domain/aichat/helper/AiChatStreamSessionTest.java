package com.storix.api.domain.aichat.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;
import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import com.storix.domain.domains.works.service.WorksCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] 답변 스트림 저장")
class AiChatStreamSessionTest {

    private static final AiChatQuestion QUESTION =
            new AiChatQuestion(1L, 10L, 100L, "로판 추천", false, Set.of());

    @Mock
    private SseEmitter emitter;
    @Mock
    private AiChatMessageService aiChatMessageService;
    @Mock
    private WorksCardService worksCardService;

    private AiChatStreamSession session;

    @BeforeEach
    void setUp() {
        session = new AiChatStreamSession(QUESTION, emitter, aiChatMessageService, worksCardService,
                new AiChatWorksJsonHelper(new ObjectMapper()));
    }

    private static WorksCardInfo card(long id) {
        return new WorksCardInfo(id, "작품 " + id, null, WorksType.WEBTOON, Genre.ROFAN, List.of(Platform.KAKAO_PAGE), false);
    }

    private static AiChatRawWork raw(int rank, long id) {
        return new AiChatRawWork(rank, id, "이유", List.of(new AiChatMatchedClue("로판", "GENRE")));
    }

    @Test
    @DisplayName("글자를 모아 답변으로 저장하고, DB 에 없는 작품은 빼고 저장한다")
    void saveAnswerWithKnownWorks() throws IOException {
        given(worksCardService.findCards(any())).willReturn(Map.of(1L, card(1L)));
        given(aiChatMessageService.saveAnswer(eq(10L), anyString(), any(), any(), any())).willReturn(200L);

        session.token("조건에 가까운 ");
        session.token("작품이에요.");
        session.works(List.of(raw(1, 1L), raw(2, 999L)), "mock");
        session.done(AiChatAnswerStatus.OK);

        ArgumentCaptor<String> worksJson = ArgumentCaptor.forClass(String.class);
        verify(aiChatMessageService).saveAnswer(eq(10L), eq("조건에 가까운 작품이에요."), eq(AiChatAnswerStatus.OK),
                worksJson.capture(), eq("mock"));
        assertThat(worksJson.getValue()).contains("\"workId\":1").doesNotContain("999");
        assertThat(session.isFinished()).isTrue();
        verify(emitter).complete();
    }

    @Test
    @DisplayName("19금을 허용하지 않았는데 온 19금 작품은 빼고, 카드가 다 빠지면 결과 없음으로 저장한다")
    void dropAdultWorksWhenNotAllowed() {
        WorksCardInfo adult = new WorksCardInfo(5L, "19금", null, WorksType.WEBTOON, Genre.ROFAN, List.of(Platform.KAKAO_PAGE), true);
        given(worksCardService.findCards(any())).willReturn(Map.of(5L, adult));
        given(aiChatMessageService.saveAnswer(eq(10L), anyString(), any(), isNull(), any())).willReturn(200L);

        session.token("찾았어요");
        session.works(List.of(raw(1, 5L)), "v1");
        session.done(AiChatAnswerStatus.OK);

        verify(aiChatMessageService).saveAnswer(10L, "찾았어요", AiChatAnswerStatus.NO_RESULT, null, "v1");
    }

    @Test
    @DisplayName("앱 연결이 끊겨도 답변은 끝까지 저장한다")
    void saveEvenWhenDisconnected() throws IOException {
        willThrow(new IOException("끊김")).given(emitter).send(any(SseEmitter.SseEventBuilder.class));
        given(aiChatMessageService.saveAnswer(eq(10L), anyString(), any(), isNull(), isNull())).willReturn(200L);

        session.token("답변");
        session.done(AiChatAnswerStatus.NO_RESULT);

        verify(aiChatMessageService).saveAnswer(10L, "답변", AiChatAnswerStatus.NO_RESULT, null, null);
        verify(emitter, never()).complete();
    }

    @Test
    @DisplayName("오류는 ERROR 답변으로 남긴다")
    void errorIsSaved() {
        session.error("TIMEOUT", "잠시 후 다시 시도해 주세요.");

        verify(aiChatMessageService).saveAnswer(10L, "잠시 후 다시 시도해 주세요.", AiChatAnswerStatus.ERROR, null, null);
        assertThat(session.isFinished()).isTrue();
    }

    @Test
    @DisplayName("답변 저장이 실패해도 스트림은 닫는다")
    void closeWhenSaveFails() throws IOException {
        given(aiChatMessageService.saveAnswer(eq(10L), anyString(), any(), any(), any()))
                .willThrow(new IllegalStateException("DB"));

        session.done(AiChatAnswerStatus.OK);

        assertThat(session.isFinished()).isTrue();
        verify(emitter).complete();
    }
}
