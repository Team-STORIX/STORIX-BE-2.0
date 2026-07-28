package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceCheckInResponse;
import com.storix.domain.domains.event.dto.AttendanceStatusResponse;
import com.storix.domain.domains.event.service.AttendanceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class AttendanceEventUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AttendanceEventService attendanceEventService;

    // 출석 현황 조회
    public CustomResponse<AttendanceStatusResponse> getStatus(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.ATTENDANCE_EVENT_LOAD_SUCCESS,
                attendanceEventService.getStatus(userId, LocalDateTime.now(KST))
        );
    }

    // 출석 체크
    public CustomResponse<AttendanceCheckInResponse> checkIn(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.ATTENDANCE_EVENT_CHECK_IN_SUCCESS,
                attendanceEventService.checkIn(userId, LocalDateTime.now(KST))
        );
    }
}
