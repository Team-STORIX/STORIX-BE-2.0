package com.storix.api.domain.user.controller.dto;

import lombok.Builder;

@Builder
public record OAuthLoginWithTokenResponse(
        String onboardingToken
) {
}
