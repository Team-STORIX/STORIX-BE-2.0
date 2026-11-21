package com.storix.api.domain.aichat.onboarding;

import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.dto.AiChatOnboardingLine;
import com.storix.domain.domains.aichat.exception.AiChatInvalidOnboardingVersionException;
import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[AI 채팅] 온보딩 기록")
class AiChatOnboardingScriptTest {

    @Test
    @DisplayName("성인 인증 사용자는 19금 답까지 챗봇 · 사용자 말풍선이 번갈아 남는다")
    void transcriptForAdult() {
        List<AiChatOnboardingLine> lines = AiChatOnboardingScript.transcript(true, true,
                List.of(WorksType.BOOK, WorksType.WEBTOON));

        assertThat(lines).extracting(AiChatOnboardingLine::role).containsExactly(
                AiChatMessageRole.CHATBOT, AiChatMessageRole.CHATBOT, AiChatMessageRole.USER,
                AiChatMessageRole.CHATBOT, AiChatMessageRole.USER, AiChatMessageRole.CHATBOT);
        assertThat(lines.get(2).content()).isEqualTo("같이 추천받기");
        assertThat(lines.get(4).content()).isEqualTo("웹툰, 단행본");
    }

    @Test
    @DisplayName("인증 전 사용자는 19금 질문이 없고, 유형을 안 고르면 상관없음")
    void transcriptForMinor() {
        List<AiChatOnboardingLine> lines = AiChatOnboardingScript.transcript(false, false, List.of());

        assertThat(lines).hasSize(4);
        assertThat(lines.get(2)).isEqualTo(new AiChatOnboardingLine(AiChatMessageRole.USER, "상관없음"));
    }

    @Test
    @DisplayName("지금 버전이 아니면 거절한다")
    void rejectOldVersion() {
        assertThatThrownBy(() -> AiChatOnboardingScript.requireCurrentVersion(AiChatOnboardingScript.VERSION + 1))
                .isSameAs(AiChatInvalidOnboardingVersionException.EXCEPTION);
    }
}
