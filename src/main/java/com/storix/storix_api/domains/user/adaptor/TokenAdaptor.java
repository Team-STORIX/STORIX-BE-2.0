package com.storix.storix_api.domains.user.adaptor;

import com.storix.storix_api.domains.user.domain.OnboardingToken;
import com.storix.storix_api.domains.user.domain.RefreshToken;
import com.storix.storix_api.domains.user.dto.OnboardingPrincipal;
import com.storix.storix_api.domains.user.repository.OnboardingTokenRepository;
import com.storix.storix_api.domains.user.repository.RefreshTokenRepository;
import com.storix.storix_api.global.apiPayload.exception.user.InvalidLogoutException;
import com.storix.storix_api.global.apiPayload.exception.user.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenAdaptor {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OnboardingTokenRepository onboardingTokenRepository;

    // RefreshToken
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        Optional<RefreshToken> refreshTokenInfo = refreshTokenRepository.findById(userId);
        if (refreshTokenInfo.isEmpty()) {
            throw InvalidLogoutException.EXCEPTION;
        }
        refreshTokenRepository.deleteById(userId);
    }

    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    // OnboardingToken
    public void saveOnboardingToken(OnboardingToken onboardingToken) {
        onboardingTokenRepository.save(onboardingToken);
    }

    public OnboardingPrincipal findOnboardingPrincipalByJti(String jti) {
        Optional<OnboardingToken> onboardingToken = onboardingTokenRepository.findById(jti);
        if (onboardingToken.isEmpty()) {
            throw InvalidTokenException.EXCEPTION;
        }

        return new OnboardingPrincipal(onboardingToken.get().getProvider(), onboardingToken.get().getOid());
    }

    public void deleteOnboardingTokenByJti(String jti) { onboardingTokenRepository.deleteById(jti);}

}
