package com.storix.api.domain.notification.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MarketingConsentRequest(

        @Schema(description = "이벤트/혜택 알림 동의 여부 (true: 동의, false: 거부)", example = "true")
        @NotNull(message = "동의 여부를 보내주세요.")
        Boolean agreed
) {
}
