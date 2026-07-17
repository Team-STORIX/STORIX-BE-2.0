package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AttendanceStatusResponse(
        Long appEventId,

        @Schema(description = "이벤트 시작일 (yyyy-MM-dd)")
        LocalDate eventStartDate,

        @Schema(description = "이벤트 종료일 (yyyy-MM-dd)")
        LocalDate eventEndDate,

        @Schema(description = "출석한 날짜 목록 (오름차순)")
        List<LocalDate> attendedDates,

        @Schema(description = "누적 출석일 수")
        int totalAttendedDays,

        @Schema(description = "오늘 출석 완료 여부")
        boolean attendedToday,

        @Schema(description = "지금까지 발급된 응모권 수 (3일 1개, 7일 2개, 14일 5개)")
        int issuedTickets,

        @Schema(description = "이벤트 진행 중 여부 (기간 외면 false)")
        boolean eventActive
) {
}
