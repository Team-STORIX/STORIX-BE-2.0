package com.storix.domain.domains.user.event;

import com.storix.domain.domains.user.domain.Title;

import java.time.LocalDateTime;

public record TitleAcquiredEvent(
        Long userId,
        Title title,
        LocalDateTime acquiredAt
) {
}
