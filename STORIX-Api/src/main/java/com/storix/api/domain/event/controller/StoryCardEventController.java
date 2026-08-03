package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.StoryCardEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.StoryCardResponse;
import com.storix.domain.domains.event.dto.StoryCardStatusResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/story-card-event")
@RequiredArgsConstructor
@Tag(name = "오늘의 스토리 카드", description = "하루 1회 랜덤 카드 뽑기 이벤트 API")
public class StoryCardEventController {

    private final StoryCardEventUseCase storyCardEventUseCase;

    @GetMapping
    @Operation(summary = "오늘의 스토리 카드 현황 조회",
            description = "이벤트 기간, 카드 기준일(06:00 초기화), 오늘 뽑기 여부를 반환합니다. "
                    + "이미 뽑았으면 card에 오늘의 카드가 함께 담기고, 아직이면 card는 null입니다. "
                    + "진행 중인 이벤트가 없으면 404.")
    public CustomResponse<StoryCardStatusResponse> getStatus(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return storyCardEventUseCase.getStatus(authUser.getUserId());
    }

    @PostMapping("/draw")
    @Operation(summary = "카드 뽑기",
            description = "오늘의 카드를 뽑습니다. 같은 날 다시 요청하면 처음 뽑은 카드를 alreadyDrawn=true로 그대로 반환합니다. 기간 외 요청은 400.")
    public CustomResponse<StoryCardResponse> draw(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return storyCardEventUseCase.draw(authUser.getUserId());
    }
}
