package com.storix.api.domain.user.helper;

import com.storix.domain.domains.user.domain.*;
import com.storix.api.domain.user.controller.dto.LoginWithTokenResponse;
import com.storix.api.domain.user.controller.dto.OAuthLoginWithTokenResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.OnboardingTokenInfo;

import com.storix.domain.domains.user.exception.token.ExpiredTokenException;
import com.storix.domain.domains.user.exception.token.InvalidRefreshTokenException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
import com.storix.infrastructure.global.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.storix.common.utils.STORIXStatic.MILLI_TO_SECOND;

@Service
@RequiredArgsConstructor
public class TokenGenerateHelper {

    private final TokenProvider tokenProvider;
    private final TokenAdaptor tokenAdaptor;
    private final UserAdaptor userAdaptor;

    @Transactional
    public LoginWithTokenResponse generateLoginWithToken(AuthUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        String role = userDetails.getRole().getStringValue();

        String accessToken = tokenProvider.createAccessToken(userId, role);
        String refreshToken = tokenProvider.createRefreshToken(userId);

        // redis 저장
        long ttlSeconds = tokenProvider.getRefreshTokenValidityMs() / MILLI_TO_SECOND;
        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(userId)
                .refreshToken(refreshToken)
                .ttl(ttlSeconds)
                .build();
        tokenAdaptor.saveRefreshToken(newRefreshToken);

        return LoginWithTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginWithTokenResponse reissueTokens(String refreshToken) {

        try {
            tokenProvider.isRefreshToken(refreshToken);
        } catch (ExpiredTokenException | InvalidTokenException e) {
            throw InvalidRefreshTokenException.EXCEPTION;
        }

        Long userId = tokenProvider.parseRefreshToken(refreshToken);
        Role role = userAdaptor.findUserRoleByUserId(userId);

        tokenAdaptor.deleteRefreshToken(refreshToken);

        // AccessToken, RefreshToken 재발급
        return generateLoginWithToken(new AuthUserDetails(userId, role));
    }

    @Transactional
    public OAuthLoginWithTokenResponse generateOAuthLoginWithToken(OAuthInfo oAuthInfo) {

        OAuthProvider provider = oAuthInfo.getProvider();
        String oid = oAuthInfo.getOid();

        OnboardingTokenInfo oti = tokenProvider.createOnboardingToken();

        long ttlSeconds = tokenProvider.getOnboardingTokenValidityMs() / MILLI_TO_SECOND;
        OnboardingToken newOnboardingToken = OnboardingToken.builder()
                .jti(oti.jti())
                .provider(provider)
                .oid(oid)
                .onboardingToken(oti.onboardingToken())
                .ttl(ttlSeconds)
                .build();
        tokenAdaptor.saveOnboardingToken(newOnboardingToken);

        return OAuthLoginWithTokenResponse.builder()
                .onboardingToken(oti.onboardingToken())
                .build();
    }

}
