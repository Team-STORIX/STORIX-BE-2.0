package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.OAuthProvider;

public record OnboardingPrincipal(
        OAuthProvider provider,
        String oid
) {}