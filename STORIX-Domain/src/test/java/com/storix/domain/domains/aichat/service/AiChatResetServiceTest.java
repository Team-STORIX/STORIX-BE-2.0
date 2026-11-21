package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatSettingAdaptor;
import com.storix.domain.domains.aichat.domain.AiChat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] 테스터 초기화")
class AiChatResetServiceTest {

    @Mock
    private AiChatAdaptor aiChatAdaptor;
    @Mock
    private AiChatMessageAdaptor aiChatMessageAdaptor;
    @Mock
    private AiChatSettingAdaptor aiChatSettingAdaptor;
    @InjectMocks
    private AiChatResetService aiChatResetService;

    @Test
    @DisplayName("대화방이 있으면 메시지 · 대화방 · 설정을 모두 지운다")
    void resetAll() {
        AiChat chat = AiChat.of(1L);
        ReflectionTestUtils.setField(chat, "id", 10L);
        given(aiChatAdaptor.findByUserId(1L)).willReturn(Optional.of(chat));

        aiChatResetService.reset(1L);

        verify(aiChatMessageAdaptor).deleteAllByChatId(10L);
        verify(aiChatAdaptor).deleteById(10L);
        verify(aiChatSettingAdaptor).deleteByUserId(1L);
    }

    @Test
    @DisplayName("대화방이 없으면 설정만 지운다")
    void resetSettingOnly() {
        given(aiChatAdaptor.findByUserId(1L)).willReturn(Optional.empty());

        aiChatResetService.reset(1L);

        verify(aiChatMessageAdaptor, never()).deleteAllByChatId(anyLong());
        verify(aiChatAdaptor, never()).deleteById(anyLong());
        verify(aiChatSettingAdaptor).deleteByUserId(1L);
    }
}
