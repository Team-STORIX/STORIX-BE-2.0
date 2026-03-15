package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OnboardingUserDetails {
    private String jti;
    private OAuthProvider provider;
    private String oid;
}