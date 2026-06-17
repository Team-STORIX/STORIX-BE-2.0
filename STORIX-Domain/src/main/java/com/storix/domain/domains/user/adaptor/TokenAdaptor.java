package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.OnboardingToken;
import com.storix.domain.domains.user.domain.RefreshToken;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.repository.OnboardingTokenRepository;
import com.storix.domain.domains.user.repository.RefreshTokenRepository;
import com.storix.domain.domains.user.exception.auth.InvalidLogoutException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
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

    // 토큰이 없어도 예외 없이 삭제 (계정 정지 시 사용)
    public void deleteRefreshTokenByUserIdIfPresent(Long userId) {
        refreshTokenRepository.findById(userId)
                .ifPresent(refreshTokenRepository::delete);
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

        return new OnboardingPrincipal(
                onboardingToken.get().getProvider(),
                onboardingToken.get().getOid(),
                onboardingToken.get().getOauthRefreshToken());
    }

    public void deleteOnboardingTokenByJti(String jti) { onboardingTokenRepository.deleteById(jti);}

}
