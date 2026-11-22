package com.storix.api.domain.aichat.usecase;

import com.storix.api.domain.aichat.controller.dto.AiChatMessageRequest;
import com.storix.api.domain.aichat.stream.AiChatContext;
import com.storix.api.domain.aichat.stream.AiChatQuestion;
import com.storix.api.domain.aichat.stream.AiChatStreamer;
import com.storix.api.domain.aichat.stream.AiChatWorksJson;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.adultverification.domain.AdultVerificationState;
import com.storix.domain.domains.adultverification.service.AdultVerificationService;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import com.storix.domain.domains.aichat.dto.AiChatQuestionInfo;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.aichat.service.AiChatMessageService;
import com.storix.domain.domains.aichat.service.AiChatSettingService;
import com.storix.domain.domains.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@UseCase
@Profile("!prod")
@RequiredArgsConstructor
public class AiChatMessageUseCase {

    private final AiChatMessageService aiChatMessageService;
    private final AiChatSettingService aiChatSettingService;
    private final AdultVerificationService adultVerificationService;
    private final LibraryService libraryService;
    private final AiChatWorksJson aiChatWorksJson;
    private final AiChatStreamer aiChatStreamer;

    // 19금 허용은 질문마다 설정 + 지금 성인 인증으로 재판단
    // 대화 맥락은 방금 저장한 질문 직전까지
    public SseEmitter send(Long userId, AiChatMessageRequest request) {
        AiChatSettingInfo setting = aiChatSettingService.find(userId);
        boolean adultVerified = adultVerificationService.getStatus(userId).state() == AdultVerificationState.VERIFIED;
        AiChatQuestionInfo saved = aiChatMessageService.saveQuestion(userId, request.question());
        List<AiChatMessageInfo> recent = aiChatMessageService.findContext(saved.chatId(), saved.messageId(),
                AiChatContext.MAX_TURNS);
        AiChatContext context = AiChatContext.of(recent, aiChatWorksJson.readAll(recent));
        List<Long> libraryWorkIds = libraryService.findReviewedWorksIds(userId);

        return aiChatStreamer.start(AiChatQuestion.of(userId, saved, request.question(), setting, adultVerified,
                libraryWorkIds, context));
    }
}
