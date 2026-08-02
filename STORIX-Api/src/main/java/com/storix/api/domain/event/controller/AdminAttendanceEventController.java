package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.controller.dto.AttendanceDrawRequest;
import com.storix.api.domain.event.usecase.AdminAttendanceEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/attendance-events")
@RequiredArgsConstructor
@Tag(name = "관리자 출석 이벤트", description = "관리자 출석 이벤트 추첨 API")
public class AdminAttendanceEventController {

    private final AdminAttendanceEventUseCase adminAttendanceEventUseCase;

    @PostMapping("/{appEventId}/draw")
    @Operation(
            summary = "출석 이벤트 당첨자 추첨",
            description = """
                    응모권 수를 가중치로 당첨자를 뽑아 1위부터 순위순으로 반환합니다.
                    응모권이 많을수록 당첨 확률이 높고, 한 유저가 두 번 당첨되지는 않습니다.
                    결과를 저장하지 않으므로 호출할 때마다 다시 추첨되며, 이벤트 진행 중에도 호출할 수 있습니다.
                    추첨 모수는 응모권 1장 이상을 보유한 정상 계정(탈퇴/정지/관리자 제외)이고,
                    모수가 요청 인원보다 적으면 있는 만큼만 반환합니다. 존재하지 않는 이벤트면 404.
                    """
    )
    public CustomResponse<AttendanceDrawResponse> draw(
            @PathVariable Long appEventId,
            @Valid @RequestBody AttendanceDrawRequest req
    ) {
        return adminAttendanceEventUseCase.draw(appEventId, req);
    }
}
