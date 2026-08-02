package com.storix.domain.domains.event.dto;

// 추첨 후보 집계용 - 유저별 누적 출석일 수
public record AttendanceAttendeeCount(
        Long userId,
        Long attendedDays
) {
}
