package com.storix.api.domain.aichat.controller.dto;

import com.storix.api.domain.aichat.helper.AiChatOnboardingHelper;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] 진입 응답")
class AiChatEntryResponseTest {

    private static final AiChatSettingInfo NEVER_SEEN = new AiChatSettingInfo(false, Set.of(), null);

    @Test
    @DisplayName("온보딩을 안 봤으면 온보딩과 예시 질문을 같이 내려준다")
    void onboardingWhenNeverSeen() {
        AiChatEntryResponse response = AiChatEntryResponse.of(NEVER_SEEN, false);

        assertThat(response.onboarding()).isNotNull();
        assertThat(response.onboarding().version()).isEqualTo(AiChatOnboardingHelper.VERSION);
        assertThat(response.exampleQuestions()).isNotEmpty();
    }

    @Test
    @DisplayName("성인 인증 전이면 19금 질문을 건너뛴다")
    void skipAdultStepWhenNotVerified() {
        AiChatEntryResponse response = AiChatEntryResponse.of(NEVER_SEEN, false);

        assertThat(response.onboarding().steps())
                .extracting(AiChatOnboardingStep::step)
                .containsExactly("GREETING", "WORKS_TYPE", "DONE");
    }

    @Test
    @DisplayName("성인 인증된 사용자에게는 19금 질문을 보여 준다")
    void showAdultStepWhenVerified() {
        AiChatEntryResponse response = AiChatEntryResponse.of(NEVER_SEEN, true);

        assertThat(response.onboarding().steps())
                .extracting(AiChatOnboardingStep::step)
                .containsExactly("GREETING", "ADULT", "WORKS_TYPE", "DONE");
    }

    @Test
    @DisplayName("작품 유형은 여러 개 고를 수 있고 상관없음이 있다")
    void worksTypeIsMultiSelect() {
        AiChatOnboardingStep worksType = AiChatEntryResponse.of(NEVER_SEEN, false).onboarding().steps().stream()
                .filter(step -> step.step().equals("WORKS_TYPE"))
                .findFirst()
                .orElseThrow();

        assertThat(worksType.multiSelect()).isTrue();
        assertThat(worksType.options()).extracting(AiChatOnboardingOption::value)
                .containsExactly("WEBTOON", "WEBNOVEL", "BOOK", "ANY");
    }

    @Test
    @DisplayName("지금 버전 온보딩을 봤으면 온보딩 없이 예시 질문만")
    void noOnboardingWhenSeen() {
        AiChatSettingInfo seen = new AiChatSettingInfo(false, Set.of(), AiChatOnboardingHelper.VERSION);

        AiChatEntryResponse response = AiChatEntryResponse.of(seen, true);

        assertThat(response.onboarding()).isNull();
        assertThat(response.exampleQuestions()).isNotEmpty();
    }
}
