package com.storix.storix_api.domains.user.controller.dto;

import lombok.Builder;

@Builder
public record OAuthLoginWithTokenResponse(
        String onboardingToken
) {
}
