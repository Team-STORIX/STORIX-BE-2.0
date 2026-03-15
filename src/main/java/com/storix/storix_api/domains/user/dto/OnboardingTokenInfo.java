package com.storix.storix_api.domains.user.dto;

public record OnboardingTokenInfo(
        String onboardingToken,
        String jti
) {
}
