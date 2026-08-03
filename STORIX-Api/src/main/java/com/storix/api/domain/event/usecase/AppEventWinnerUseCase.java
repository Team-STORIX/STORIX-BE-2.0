package com.storix.api.domain.event.usecase;

import com.storix.api.domain.event.controller.dto.AppEventWinnerDrawRequest;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.dto.AppEventWinnerDrawResponse;
import com.storix.domain.domains.event.service.winner.AppEventFinalizeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@UseCase
@RequiredArgsConstructor
public class AppEventWinnerUseCase {

    private final AppEventFinalizeService appEventFinalizeService;

    // 당첨자 이벤트(hasWinner=true) 공통 당첨자 확정 (이미 확정된 이벤트면 저장된 당첨자를 그대로 반환)
    public CustomResponse<AppEventWinnerDrawResponse> draw(Long appEventId, AppEventWinnerDrawRequest req) {
        MDC.put(STORIXStatic.Mdc.APP_EVENT_ID, String.valueOf(appEventId));

        return CustomResponse.onSuccess(
                SuccessCode.APP_EVENT_WINNER_DRAW_SUCCESS,
                appEventFinalizeService.finalizeWinners(appEventId, req.winnerCount()));
    }
}
