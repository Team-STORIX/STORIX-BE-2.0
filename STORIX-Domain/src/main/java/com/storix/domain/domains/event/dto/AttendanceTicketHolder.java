package com.storix.domain.domains.event.dto;

// 추첨 후보 - 응모권을 1장 이상 보유한 참여자. ticketCount 가 추첨 가중치가 된다.
public record AttendanceTicketHolder(
        Long userId,
        int totalAttendedDays,
        int ticketCount
) {
}
