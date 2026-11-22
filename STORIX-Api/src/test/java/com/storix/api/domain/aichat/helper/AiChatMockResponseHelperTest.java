package com.storix.api.domain.aichat.helper;

import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import com.storix.domain.domains.works.service.WorksCardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] mock 답변")
class AiChatMockResponseHelperTest {

    private static final long BAD_BOSS = 4L;

    @Mock
    private WorksCardService worksCardService;
    @InjectMocks
    private AiChatMockResponseHelper responder;

    private final RecordingSink sink = new RecordingSink();

    private void givenWorks() {
        lenient().when(worksCardService.findIdByTitle(anyString(), any())).thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
            case "치즈인더트랩" -> Optional.of(1L);
            case "어두운 바다의 등불이 되어" -> Optional.of(2L);
            case "전지적 독자 시점" -> Optional.of(3L);
            case "나쁜 상사" -> Optional.of(BAD_BOSS);
            default -> Optional.empty();
        });
        lenient().when(worksCardService.findCards(anyCollection())).thenAnswer(invocation -> {
            java.util.Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().collect(Collectors.toMap(id -> id, id ->
                    new WorksCardInfo(id, "작품" + id, null, WorksType.WEBTOON, Genre.ROFAN, List.of(), id == BAD_BOSS)));
        });
        lenient().when(worksCardService.pickRandomIds(anyList(), anyInt(), any(Boolean.class)))
                .thenAnswer(invocation -> {
                    int needed = invocation.getArgument(1);
                    List<Long> ids = new ArrayList<>();
                    for (int i = 0; i < needed; i++) ids.add(100L + i);
                    return ids;
                });
    }

    private static AiChatQuestion question(String text, boolean adultDefault) {
        return new AiChatQuestion(1L, 10L, 100L, text, adultDefault, adultDefault, Set.of());
    }

    @Test
    @DisplayName("19금이 허용되면 정해 둔 작품 4개 + 무작위 1개")
    void adultAllowedIncludesBlackSwan() throws InterruptedException {
        givenWorks();

        responder.respond(question("로판 추천", true), sink);

        assertThat(sink.workIds).containsExactly(1L, 2L, 3L, BAD_BOSS, 100L);
        assertThat(sink.status).isEqualTo(AiChatAnswerStatus.OK);
    }

    @Test
    @DisplayName("19금이 허용되지 않으면 나쁜 상사를 빼고 무작위로 채운다")
    void adultNotAllowedExcludesBlackSwan() throws InterruptedException {
        givenWorks();

        responder.respond(question("로판 추천", false), sink);

        assertThat(sink.workIds).containsExactly(1L, 2L, 3L, 100L, 101L);
    }

    @Test
    @DisplayName("질문에 19금 조건이 있으면 설정보다 먼저 따른다")
    void statedConditionOverridesSetting() throws InterruptedException {
        givenWorks();

        responder.respond(question("19금도 같이 로판 추천", false), sink);
        assertThat(sink.workIds).contains(BAD_BOSS);

        givenWorks();
        responder.respond(question("19금 빼고 로판 추천", true), sink);
        assertThat(sink.workIds).doesNotContain(BAD_BOSS);
    }

    @Test
    @DisplayName("1개 를 넣으면 카드 1개")
    void singleCard() throws InterruptedException {
        givenWorks();

        responder.respond(question("1개만", false), sink);

        assertThat(sink.workIds).containsExactly(1L);
    }

    @Test
    @DisplayName("결과없음 은 카드 없이 NO_RESULT, 오류 는 error")
    void keywords() throws InterruptedException {
        responder.respond(question("결과없음", false), sink);
        assertThat(sink.status).isEqualTo(AiChatAnswerStatus.NO_RESULT);
        assertThat(sink.workIds).isEmpty();

        RecordingSink errorSink = new RecordingSink();
        responder.respond(question("오류", false), errorSink);
        assertThat(errorSink.errorCode).isEqualTo("TIMEOUT");
        assertThat(errorSink.status).isNull();
    }

    private static class RecordingSink implements AiChatAnswerSink {
        private List<Long> workIds = List.of();
        private AiChatAnswerStatus status;
        private String errorCode;

        @Override public void progress(String step, String message) { }
        @Override public void token(String text) { }
        @Override public void works(List<AiChatRawWork> works, String pipelineVersion) {
            this.workIds = works.stream().map(AiChatRawWork::workId).toList();
        }
        @Override public void done(AiChatAnswerStatus status) { this.status = status; }
        @Override public void error(String code, String message) { this.errorCode = code; }
    }
}
