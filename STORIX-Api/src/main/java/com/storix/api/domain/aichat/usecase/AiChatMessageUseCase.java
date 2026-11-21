package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.stream.AiChatQuestion;
import com.storix.api.domain.aichat.stream.AiChatStreamer;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.aichat.service.AiChatSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 답변은 아직 mock 이 만들어서 prod 에는 없다. AI 서버를 붙이면 Profile 을 뗀다
@UseCase
@Profile("!prod")
@RequiredArgsConstructor
public class AiChatMessageUseCase {

    private final AiChatMessageService aiChatMessageService;
    private final AiChatSettingService aiChatSettingService;
    private final AdultVerificationService adultVerificationService;
    private final AiChatStreamer aiChatStreamer;

    // 19금 허용은 질문마다 설정 + 지금 성인 인증으로 다시 판단한다
    public SseEmitter send(Long userId, AiChatMessageRequest request) {
        AiChatSettingInfo setting = aiChatSettingService.find(userId);
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;
        AiChatQuestionInfo saved = aiChatMessageService.saveQuestion(userId, request.question());

        return aiChatStreamer.start(AiChatQuestion.of(userId, saved, request.question(), setting, adultVerified));
    }
}
