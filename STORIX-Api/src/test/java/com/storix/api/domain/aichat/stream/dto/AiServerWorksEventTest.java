package com.storix.api.domain.aichat.stream.dto;

import com.storix.api.domain.aichat.stream.AiChatRawWork;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] AI 추천 작품 확인")
class AiServerWorksEventTest {

    private static AiServerRecommendedWork work(int rank, Long id) {
        return new AiServerRecommendedWork(rank, id, "이유 " + id, null);
    }

    @Test
    @DisplayName("후보 밖 · 서재 · 중복 작품은 버리고 순위를 1 부터 다시 매긴다")
    void keepOnlyTrusted() {
        AiServerWorksEvent event = new AiServerWorksEvent(
                List.of(work(3, 3L), work(1, 1L), work(2, 99L), work(4, 1L), work(5, 4L), work(6, null)),
                List.of(1L, 3L, 4L, 5L),
                "v1");

        List<AiChatRawWork> trusted = event.trustedWorks(List.of(4L), 5);

        assertThat(trusted).extracting(AiChatRawWork::workId).containsExactly(1L, 3L);
        assertThat(trusted).extracting(AiChatRawWork::rank).containsExactly(1, 2);
        assertThat(trusted.get(0).matchedClues()).isEmpty();
    }

    @Test
    @DisplayName("최대 개수까지만 남긴다")
    void limitToMax() {
        AiServerWorksEvent event = new AiServerWorksEvent(
                List.of(work(1, 1L), work(2, 2L), work(3, 3L)),
                List.of(1L, 2L, 3L),
                "v1");

        assertThat(event.trustedWorks(List.of(), 2)).extracting(AiChatRawWork::workId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("추천 · 후보가 비어 오면 빈 목록")
    void emptyWhenNull() {
        assertThat(new AiServerWorksEvent(null, null, "v1").trustedWorks(List.of(), 5)).isEmpty();
    }
}
