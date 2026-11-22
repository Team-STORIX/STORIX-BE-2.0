package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;
import com.storix.api.domain.aichat.helper.AiChatStreamHelper;
import com.storix.api.domain.aichat.helper.AiChatUsageLimitHelper;
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

@UseCase
@Profile("!prod")
@RequiredArgsConstructor
public class AiChatMessageUseCase {

    private final AiChatMessageService aiChatMessageService;
    private final AiChatSettingService aiChatSettingService;
    private final AdultVerificationService adultVerificationService;
    private final AiChatUsageLimitHelper aiChatUsageLimitHelper;
    private final AiChatStreamHelper aiChatStreamHelper;

    public SseEmitter send(Long userId, AiChatMessageRequest request) {
        aiChatUsageLimitHelper.check(userId);
        AiChatSettingInfo setting = aiChatSettingService.find(userId);
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;
        AiChatQuestionInfo saved = aiChatMessageService.saveQuestion(userId, request.question());

        return aiChatStreamHelper.start(AiChatQuestion.of(userId, saved, request.question(), setting, adultVerified));
    }
}
