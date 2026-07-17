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

        @Schema(description = "이번 출석으로 새로 발급된 응모권 수 (3·7·14일 달성 시에만 1)")
        int newlyIssuedTickets,

        @Schema(description = "지금까지 발급된 응모권 수")
        int issuedTickets
) {
}
