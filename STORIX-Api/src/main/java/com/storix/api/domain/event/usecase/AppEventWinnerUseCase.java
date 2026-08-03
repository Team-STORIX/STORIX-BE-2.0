package com.storix.api.domain.event.usecase;

import com.storix.api.domain.event.controller.dto.AppEventWinnerDrawRequest;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AppEventWinnerDrawResponse;
import com.storix.domain.domains.event.service.winner.AppEventFinalizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AppEventWinnerUseCase {

    private final AppEventFinalizeService appEventFinalizeService;

    // 당첨자 이벤트(hasWinner=true) 공통 당첨자 확정 (이미 확정된 이벤트면 저장된 당첨자를 그대로 반환)
    public CustomResponse<AppEventWinnerDrawResponse> draw(Long appEventId, AppEventWinnerDrawRequest req) {

        AppEventWinnerDrawResponse result = appEventFinalizeService.finalizeWinners(appEventId, req.winnerCount());
        log.info(">>> [AppEvent] 당첨자 확정 appEventId={}, 당첨={}명, 기확정={}",
                appEventId, result.winners().size(), result.alreadyFinalized());
        return CustomResponse.onSuccess(SuccessCode.APP_EVENT_WINNER_DRAW_SUCCESS, result);
    }
}
