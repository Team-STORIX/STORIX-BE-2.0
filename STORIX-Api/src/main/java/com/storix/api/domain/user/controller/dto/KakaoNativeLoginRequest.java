package com.storix.api.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoNativeLoginRequest(
        @NotBlank(message = "accessToken은 필수입니다.")
        String accessToken,

        @NotBlank(message = "idToken은 필수입니다.")
        String idToken
) {
}
