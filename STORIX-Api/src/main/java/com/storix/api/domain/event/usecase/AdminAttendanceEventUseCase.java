package com.storix.api.domain.event.usecase;

import com.storix.api.domain.event.controller.dto.AttendanceDrawRequest;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.service.AttendanceDrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAttendanceEventUseCase {

    private final AttendanceDrawService attendanceDrawService;

    // 출석 이벤트 당첨자 추첨/확정 (이미 확정된 이벤트면 저장된 당첨자를 그대로 반환)
    public CustomResponse<AttendanceDrawResponse> draw(Long appEventId, AttendanceDrawRequest req) {

        AttendanceDrawResponse result = attendanceDrawService.draw(appEventId, req.winnerCount());
        log.info(">>> [AttendanceEvent] 당첨자 확정 appEventId={}, 모수={}명/{}장, 당첨={}명",
                appEventId, result.candidateCount(), result.totalTickets(), result.winners().size());
        return CustomResponse.onSuccess(SuccessCode.ATTENDANCE_EVENT_DRAW_SUCCESS, result);
    }

    // 확정된 당첨자 조회 (추첨을 유발하지 않는 조회 전용)
    public CustomResponse<AttendanceDrawResponse> getWinners(Long appEventId) {

        AttendanceDrawResponse result = attendanceDrawService.findWinners(appEventId);
        return CustomResponse.onSuccess(SuccessCode.ATTENDANCE_EVENT_WINNERS_LOAD_SUCCESS, result);
    }
}
