package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceCheckInResponse;
import com.storix.domain.domains.event.dto.AttendanceStatusResponse;
import com.storix.domain.domains.event.service.AttendanceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AttendanceEventUseCase {

    private final AttendanceEventService attendanceEventService;

    // 현재 진행 중인 출석 이벤트의 app_event_id (미설정 시 0 → 404)
    @Value("${attendance-event.app-event-id:0}") private Long attendanceAppEventId;

    // 출석 현황 조회
    public CustomResponse<AttendanceStatusResponse> getStatus(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.ATTENDANCE_EVENT_LOAD_SUCCESS,
                attendanceEventService.getStatus(attendanceAppEventId, userId, LocalDate.now())
        );
    }

    // 출석 체크
    public CustomResponse<AttendanceCheckInResponse> checkIn(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.ATTENDANCE_EVENT_CHECK_IN_SUCCESS,
                attendanceEventService.checkIn(attendanceAppEventId, userId, LocalDate.now())
        );
    }
}
