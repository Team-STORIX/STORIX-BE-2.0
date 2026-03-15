package com.storix.storix_api.domains.user.dto;

import com.storix.storix_api.domains.user.domain.OAuthProvider;

public record OnboardingPrincipal(
        OAuthProvider provider,
        String oid
) {}