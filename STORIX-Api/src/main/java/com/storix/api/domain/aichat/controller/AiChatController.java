package com.storix.api.domain.aichat.controller;

import com.storix.api.domain.aichat.controller.dto.AiChatEntryResponse;
import com.storix.api.domain.aichat.controller.dto.AiChatMessagesResponse;
import com.storix.api.domain.aichat.controller.dto.AiChatSettingRequest;
import com.storix.api.domain.aichat.controller.dto.AiChatSettingResponse;
import com.storix.api.domain.aichat.usecase.AiChatEntryUseCase;
import com.storix.api.domain.aichat.usecase.AiChatHistoryUseCase;
import com.storix.api.domain.aichat.usecase.AiChatSettingUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI 작품 검색", description = "AI 작품 검색 API")
public class AiChatController {

    private final AiChatEntryUseCase aiChatEntryUseCase;
    private final AiChatSettingUseCase aiChatSettingUseCase;
    private final AiChatHistoryUseCase aiChatHistoryUseCase;

    @GetMapping
    @Operation(summary = "[1] 진입", description = "AI 작품 검색 화면에 들어올 때 호출해주세요.   \n" +
            "대화방은 사용자당 하나라 따로 만들지 않습니다.   \n\n" +
            "**onboarding 이 있으면** steps 를 순서대로 말풍선으로 보여 주세요.   \n" +
            "- 선택지가 있는 단계(ADULT · WORKS_TYPE)는 고른 값을 모아 마지막에 [3] 챗봇 설정 저장으로 보냅니다. 이때 onboardingVersion 에 onboarding.version 을 그대로 넣어주세요   \n" +
            "- ADULT 단계는 성인 인증된 사용자에게만 내려갑니다. 없으면 19금은 false 로 저장합니다   \n" +
            "- WORKS_TYPE 에서 ANY(상관없음)를 고르면 worksTypes 를 빈 배열로 보냅니다   \n" +
            "- 건너뛰기는 adultEnabled false · worksTypes 빈 배열로 저장합니다   \n\n" +
            "**onboarding 이 null 이면** [4] 지난 대화를 불러와 바로 이어 가면 됩니다.   \n" +
            "exampleQuestions 는 입력창 위 예시 질문입니다.")
    public CustomResponse<AiChatEntryResponse> enter(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return aiChatEntryUseCase.enter(authUser.getUserId());
    }

    @GetMapping("/settings")
    @Operation(summary = "[2] 챗봇 설정 조회", description = "설정 화면에서 호출해주세요.   \n" +
            "adultSelectable 이 false 면(성인 인증 전 · 만료) 19금 토글을 끈 채 비활성화해주세요.")
    public CustomResponse<AiChatSettingResponse> findSetting(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return aiChatSettingUseCase.find(authUser.getUserId());
    }

    @PutMapping("/settings")
    @Operation(summary = "[3] 챗봇 설정 저장", description = "온보딩 마지막 단계와 설정 화면이 같이 씁니다.   \n" +
            "- 온보딩에서 저장할 때만 onboardingVersion 을 넣어주세요. 온보딩 말풍선과 고른 답이 지난 대화 맨 앞에 남고, 다음 진입부터 onboarding 이 null 입니다   \n" +
            "- 설정 화면에서는 onboardingVersion 을 비워주세요   \n" +
            "- 성인 인증 전인데 adultEnabled 를 true 로 보내면 403 (ADULT_VERIFICATION_ERROR_008)   \n" +
            "- 만화(COMIC)는 고를 수 없습니다 (AI_CHAT_ERROR_001)")
    public CustomResponse<AiChatSettingResponse> saveSetting(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody AiChatSettingRequest request
    ) {
        return aiChatSettingUseCase.save(authUser.getUserId(), request);
    }

    @GetMapping("/messages")
    @Operation(summary = "[4] 지난 대화", description = "진입 후 onboarding 이 null 이면 호출해주세요. 최근 size 개를 오래된 순으로 줍니다.   \n" +
            "위로 스크롤하면 messages 맨 앞 messageId 를 before 로 넣어 다시 부릅니다. hasNext 가 false 면 끝입니다.   \n" +
            "챗봇 답변은 status 로 화면을 고릅니다 (질문 보내기 API 설명 참고). 온보딩 기록은 type ONBOARDING 입니다.")
    public CustomResponse<AiChatMessagesResponse> findMessages(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestParam(required = false) Long before,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return aiChatHistoryUseCase.findMessages(authUser.getUserId(), before, size);
    }
}
