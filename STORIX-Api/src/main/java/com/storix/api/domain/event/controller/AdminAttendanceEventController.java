package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.AdminAttendanceEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/attendance-events")
@RequiredArgsConstructor
@Tag(name = "관리자 출석 이벤트", description = "관리자 출석 이벤트 당첨자 조회 API")
public class AdminAttendanceEventController {

    private final AdminAttendanceEventUseCase adminAttendanceEventUseCase;

    @GetMapping("/{appEventId}/winners")
    @Operation(
            summary = "출석 이벤트 확정 당첨자 조회",
            description = """
                    확정된 당첨자를 뽑힌 순서대로, 출석 이벤트의 응모권 통계와 함께 조회합니다.
                    존재하지 않거나 출석 이벤트가 아니면 404.
                    """
    )
    public CustomResponse<AttendanceDrawResponse> getWinners(@PathVariable Long appEventId) {
        return adminAttendanceEventUseCase.getWinners(appEventId);
    }
}
