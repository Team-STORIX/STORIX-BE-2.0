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

    // 출석 이벤트 당첨자 추첨
    public CustomResponse<AttendanceDrawResponse> draw(Long appEventId, AttendanceDrawRequest req) {

        AttendanceDrawResponse result = attendanceDrawService.draw(appEventId, req.winnerCount());
        log.info(">>> [AttendanceEvent] 추첨 완료 appEventId={}, 모수={}명/{}장, 당첨={}명",
                appEventId, result.candidateCount(), result.totalTickets(), result.winners().size());
        return CustomResponse.onSuccess(SuccessCode.ATTENDANCE_EVENT_DRAW_SUCCESS, result);
    }
}
