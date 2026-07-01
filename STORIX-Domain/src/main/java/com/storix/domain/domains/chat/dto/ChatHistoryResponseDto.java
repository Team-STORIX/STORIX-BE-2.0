package com.storix.domain.domains.chat.dto;

import org.springframework.data.domain.Slice;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record ChatHistoryResponseDto(
        String joinedAt,
        Slice<ChatMessageResponseDto> messages
) {
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static ChatHistoryResponseDto from(LocalDateTime joinedAt, Slice<ChatMessageResponseDto> messages) {
        long days = Duration.between(joinedAt, LocalDateTime.now(KST_ZONE_ID)).toDays() + 1;
        return new ChatHistoryResponseDto(Math.max(days, 1) + "일", messages);
    }
}
