package com.storix.storix_api.domains.user.controller.dto;

import lombok.Builder;

@Builder
public record LoginWithTokenResponse(
        String accessToken,
        String refreshToken
) {
}
