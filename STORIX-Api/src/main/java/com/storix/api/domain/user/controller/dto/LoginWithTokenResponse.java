package com.storix.api.domain.user.controller.dto;

import lombok.Builder;

@Builder
public record LoginWithTokenResponse(
        String accessToken,
        String refreshToken
) {
}
