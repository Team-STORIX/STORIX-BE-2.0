package com.storix.api.domain.adultverification.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdultVerificationConfirmRequest(
        @Schema(description = "발급 API로 받은 본인인증 번호")
        @NotBlank
        String identityVerificationId
) {
}
