package com.storix.domain.domains.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public record ChatHistoryResponseDto(
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss",
                timezone = "Asia/Seoul")
        LocalDateTime joinedAt,
        Slice<ChatMessageResponseDto> messages
) {}
