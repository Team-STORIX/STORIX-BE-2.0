package com.storix.api.domain.event.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.service.AttendanceDrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AdminAttendanceEventUseCase {

    private final AttendanceDrawService attendanceDrawService;

    // 확정된 당첨자 조회 (추첨을 유발하지 않는 조회 전용)
    public CustomResponse<AttendanceDrawResponse> getWinners(Long appEventId) {

        AttendanceDrawResponse result = attendanceDrawService.findWinners(appEventId);
        return CustomResponse.onSuccess(SuccessCode.ATTENDANCE_EVENT_WINNERS_LOAD_SUCCESS, result);
    }
}
