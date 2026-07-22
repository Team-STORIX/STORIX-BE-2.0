package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.AttendanceEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceCheckInResponse;
import com.storix.domain.domains.event.dto.AttendanceStatusResponse;
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
@RequestMapping("/api/v1/attendance-event")
@RequiredArgsConstructor
@Tag(name = "출석 이벤트", description = "출석 체크 이벤트 API")
public class AttendanceEventController {

    private final AttendanceEventUseCase attendanceEventUseCase;

    @GetMapping
    @Operation(summary = "출석 현황 조회", description = "이벤트 기간, 출석한 날짜 목록, 오늘 출석 여부, 발급된 응모권 수를 한 번에 반환합니다. 진행 중인 출석 이벤트가 없으면 404.")
    public CustomResponse<AttendanceStatusResponse> getStatus(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return attendanceEventUseCase.getStatus(authUser.getUserId());
    }

    @PostMapping("/check-in")
    @Operation(summary = "출석 체크", description = "오늘(Asia/Seoul 00:00~23:59) 출석을 기록합니다. 기간 외 요청은 400, 이미 출석한 날 재요청은 409.")
    public CustomResponse<AttendanceCheckInResponse> checkIn(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return attendanceEventUseCase.checkIn(authUser.getUserId());
    }
}
