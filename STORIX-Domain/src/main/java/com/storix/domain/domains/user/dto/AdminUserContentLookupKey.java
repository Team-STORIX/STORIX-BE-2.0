package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.report.domain.TargetContentType;

public record AdminUserContentLookupKey(
        TargetContentType type,
        Long contentId
) {
}
