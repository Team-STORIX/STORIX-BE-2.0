package com.storix.domain.domains.chat.dto;

import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ChatHistoryResponseDto(
        String joinedDays,
        Integer activeUserNumber,
        List<Long> blockedUserIds,
        Slice<ChatMessageResponseDto> messages
) {
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static ChatHistoryResponseDto from(
            LocalDateTime joinedAt,
            Integer activeUserNumber,
            List<Long> blockedUserIds,
            Slice<ChatMessageResponseDto> messages
    ) {
        LocalDate joinedDate = joinedAt.toLocalDate();
        LocalDate today = LocalDate.now(KST_ZONE_ID);
        long days = ChronoUnit.DAYS.between(joinedDate, today) + 1;

        return new ChatHistoryResponseDto(
                Math.max(days, 1) + "일",
                activeUserNumber,
                blockedUserIds,
                messages);
    }
}
