package com.storix.api.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record NaverNativeLoginRequest(
        @NotBlank(message = "accessToken은 필수입니다.")
        String accessToken
) {
}
