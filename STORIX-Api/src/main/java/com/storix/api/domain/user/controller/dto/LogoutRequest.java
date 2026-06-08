package com.storix.api.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
        @NotBlank(message = "storage에 저장된 기기 식별자를 보내주세요.")
        @Size(max = 64)
        String installationId
) {
}
