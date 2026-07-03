package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.report.domain.TargetContentType;

import java.time.LocalDateTime;

public record AdminUserContentKey(
        TargetContentType type,
        Long contentId,
        LocalDateTime createdAt
) {
}
