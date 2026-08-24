package com.storix.api.domain.adultverification.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AdultVerificationResetRequest(
        @Schema(description = "이력을 삭제할 대상 유저")
        @NotNull
        Long userId
) {
}
