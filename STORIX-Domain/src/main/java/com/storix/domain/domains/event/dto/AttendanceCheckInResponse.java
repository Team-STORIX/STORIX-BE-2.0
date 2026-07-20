package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AttendanceCheckInResponse(
        @Schema(description = "출석 처리된 날짜 (yyyy-MM-dd)")
        LocalDate attendedDate,

        @Schema(description = "누적 출석일 수")
        int totalAttendedDays,

        @Schema(description = "이번 출석으로 새로 발급된 응모권 수 (이벤트 지급표 기준, 기본표는 3일 +1/7일 +1/14일 +3)")
        int newlyIssuedTickets,

        @Schema(description = "지금까지 발급된 응모권 수")
        int issuedTickets
) {
}
