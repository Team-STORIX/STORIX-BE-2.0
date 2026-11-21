package com.storix.domain.domains.aichat.domain;

import com.storix.domain.domains.aichat.exception.AiChatUnsupportedWorksTypeException;
import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[AI 채팅] 챗봇 설정")
class AiChatSettingTest {

    @Test
    @DisplayName("기본값은 19금 끔 · 상관없음 · 온보딩 안 봄")
    void defaultValues() {
        AiChatSetting setting = AiChatSetting.defaultOf(1L);

        assertThat(setting.isAdultEnabled()).isFalse();
        assertThat(setting.getWorksTypes()).isEmpty();
        assertThat(setting.hasSeenOnboarding(1)).isFalse();
    }

    @Test
    @DisplayName("웹툰 · 웹소설 · 단행본은 고를 수 있다")
    void updateSelectableTypes() {
        AiChatSetting setting = AiChatSetting.defaultOf(1L);

        setting.update(true, Set.of(WorksType.WEBTOON, WorksType.BOOK), 1);

        assertThat(setting.isAdultEnabled()).isTrue();
        assertThat(setting.getWorksTypes()).containsExactlyInAnyOrder(WorksType.WEBTOON, WorksType.BOOK);
        assertThat(setting.hasSeenOnboarding(1)).isTrue();
    }

    @Test
    @DisplayName("만화는 온보딩 선택지에 없어서 거절한다")
    void rejectComic() {
        AiChatSetting setting = AiChatSetting.defaultOf(1L);

        assertThatThrownBy(() -> setting.update(false, Set.of(WorksType.COMIC), 1))
                .isSameAs(AiChatUnsupportedWorksTypeException.EXCEPTION);
        assertThat(setting.getWorksTypes()).isEmpty();
    }

    @Test
    @DisplayName("온보딩 버전이 오르면 다시 보여 준다")
    void onboardingVersionBump() {
        AiChatSetting setting = AiChatSetting.defaultOf(1L);
        setting.update(false, Set.of(), 1);

        assertThat(setting.hasSeenOnboarding(2)).isFalse();
    }
}
