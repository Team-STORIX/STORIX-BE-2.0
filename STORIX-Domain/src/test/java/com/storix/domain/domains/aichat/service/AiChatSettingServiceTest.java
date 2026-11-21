package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatSettingAdaptor;
import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;
import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.aichat.dto.AiChatOnboardingLine;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] 챗봇 설정 저장")
class AiChatSettingServiceTest {

    private static final List<AiChatOnboardingLine> LINES = List.of(
            new AiChatOnboardingLine(AiChatMessageRole.CHATBOT, "인사"),
            new AiChatOnboardingLine(AiChatMessageRole.USER, "웹툰"));

    @Mock
    private AiChatSettingAdaptor aiChatSettingAdaptor;
    @Mock
    private AiChatAdaptor aiChatAdaptor;
    @Mock
    private AiChatMessageAdaptor aiChatMessageAdaptor;
    @InjectMocks
    private AiChatSettingService aiChatSettingService;

    private AiChat chat() {
        AiChat chat = AiChat.of(1L);
        ReflectionTestUtils.setField(chat, "id", 10L);
        return chat;
    }

    @Test
    @DisplayName("온보딩을 처음 마치면 말풍선을 순서대로 대화 기록에 남기고 대화방을 만든다")
    void firstOnboardingWritesTranscript() {
        given(aiChatSettingAdaptor.findForUpdateOrCreate(1L)).willReturn(AiChatSetting.defaultOf(1L));
        given(aiChatAdaptor.findOrCreate(1L)).willReturn(chat());

        AiChatSettingInfo saved = aiChatSettingService.save(1L, false, Set.of(WorksType.WEBTOON), 1, LINES);

        ArgumentCaptor<AiChatMessage> captor = ArgumentCaptor.forClass(AiChatMessage.class);
        verify(aiChatMessageAdaptor, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(AiChatMessage::getChatId, AiChatMessage::getRole, AiChatMessage::getType, AiChatMessage::getContent)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, AiChatMessageRole.CHATBOT, AiChatMessageType.ONBOARDING, "인사"),
                        org.assertj.core.groups.Tuple.tuple(10L, AiChatMessageRole.USER, AiChatMessageType.ONBOARDING, "웹툰"));
        assertThat(saved.hasSeenOnboarding(1)).isTrue();
        assertThat(saved.worksTypes()).containsExactly(WorksType.WEBTOON);
    }

    @Test
    @DisplayName("이미 본 버전으로 다시 보내면 설정만 바꾸고 기록은 다시 남기지 않는다")
    void repeatedOnboardingDoesNotDuplicate() {
        AiChatSetting setting = AiChatSetting.defaultOf(1L);
        setting.completeOnboarding(1);
        given(aiChatSettingAdaptor.findForUpdateOrCreate(1L)).willReturn(setting);

        aiChatSettingService.save(1L, true, Set.of(), 1, LINES);

        verify(aiChatMessageAdaptor, never()).save(any());
        verify(aiChatAdaptor, never()).findOrCreate(any());
        assertThat(setting.isAdultEnabled()).isTrue();
    }

    @Test
    @DisplayName("설정 화면 저장(온보딩 버전 없음)은 기록을 남기지 않는다")
    void settingScreenSaveHasNoTranscript() {
        given(aiChatSettingAdaptor.findForUpdateOrCreate(1L)).willReturn(AiChatSetting.defaultOf(1L));

        AiChatSettingInfo saved = aiChatSettingService.save(1L, false, Set.of(WorksType.BOOK), null, LINES);

        verify(aiChatMessageAdaptor, never()).save(any());
        assertThat(saved.hasSeenOnboarding(1)).isFalse();
    }
}
