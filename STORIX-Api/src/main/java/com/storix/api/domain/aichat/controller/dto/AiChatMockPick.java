package com.storix.api.domain.aichat.controller.dto;

import com.storix.domain.domains.works.domain.WorksType;

public record AiChatMockPick(
        String title,
        WorksType worksType
) {
}
