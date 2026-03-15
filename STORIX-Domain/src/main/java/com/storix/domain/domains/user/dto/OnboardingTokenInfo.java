package com.storix.domain.domains.user.dto;

public record OnboardingTokenInfo(
        String onboardingToken,
        String jti
) {
}
