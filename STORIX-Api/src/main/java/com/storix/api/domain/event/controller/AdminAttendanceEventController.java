package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.controller.dto.AttendanceDrawRequest;
import com.storix.api.domain.event.usecase.AdminAttendanceEventUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
                    응모권 수를 가중치로 당첨자를 뽑아 확정하고, 뽑힌 순서대로 반환합니다.
                    응모권이 많을수록 당첨 확률이 높고, 한 유저가 두 번 당첨되지는 않습니다.

                    추첨은 이벤트당 최초 1회만 실행되고 결과가 저장됩니다.
                    이미 확정된 이벤트에 다시 호출하면 재추첨 없이 확정된 당첨자를 반환합니다.

                    단순 확인 용도라면 조회 전용인 GET /{appEventId}/winners 를 사용하세요.
                    확정된 당첨자는 관리자 알림(대상 EVENT_WINNERS) 발송 대상이 됩니다.

                    추첨 모수는 응모권 1장 이상을 보유한 정상 계정(탈퇴/정지/관리자 제외)이고,
                    이벤트 진행 중에도 호출할 수 있으나 (조기 종료 시), 확정 이후의 출석은 반영되지 않습니다.

                    존재하지 않거나 출석 이벤트가 아니면 404, 당첨자를 뽑지 않는 이벤트(hasWinner=false)면 400.
                    """
    )
    public CustomResponse<AttendanceDrawResponse> draw(
            @PathVariable Long appEventId,
            @Valid @RequestBody AttendanceDrawRequest req
    ) {
        return adminAttendanceEventUseCase.draw(appEventId, req);
    }

    @GetMapping("/{appEventId}/winners")
    @Operation(
            summary = "출석 이벤트 확정 당첨자 조회",
            description = """
                    확정된 당첨자를 뽑힌 순서대로 조회합니다. 
                    관리자 페이지의 당첨자 확인에 사용합니다.

                    아직 추첨하지 않은 이벤트는 winners가 빈 배열이고 모수 통계(candidateCount, totalTickets)만 채워집니다.
                    모수 통계와 당첨자별 응모권 수는 조회 시점 기준으로 다시 계산되므로,
                    이벤트 진행 중이라면 호출할 때마다 달라질 수 있습니다. (확정된 당첨자 명단 자체는 바뀌지 않습니다.)

                    존재하지 않거나 출석 이벤트가 아니면 404.
                    """
    )
    public CustomResponse<AttendanceDrawResponse> getWinners(@PathVariable Long appEventId) {
        return adminAttendanceEventUseCase.getWinners(appEventId);
    }
}
