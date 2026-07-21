package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.OnboardingToken;
import com.storix.domain.domains.user.domain.RefreshToken;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.repository.OnboardingTokenRepository;
import com.storix.domain.domains.user.repository.RefreshTokenRepository;
import com.storix.domain.domains.user.exception.auth.InvalidLogoutException;
import com.storix.domain.domains.user.exception.token.InvalidRefreshTokenException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenAdaptor {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OnboardingTokenRepository onboardingTokenRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";

    // 저장된 토큰과 일치할 때만 삭제하고 1을 반환. 동시 요청 중 하나만 1을 받는다.
    private static final String CONSUME_REFRESH_TOKEN_SCRIPT = """
            if redis.call('HGET', KEYS[1], ARGV[1]) == ARGV[2] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """;

    // RefreshToken
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    public void consumeRefreshToken(Long userId, String refreshToken) {
        RedisScript<Long> script = new DefaultRedisScript<>(CONSUME_REFRESH_TOKEN_SCRIPT, Long.class);

        Long consumed = redisTemplate.execute(script,
                Collections.singletonList(REFRESH_TOKEN_KEY_PREFIX + userId),
                REFRESH_TOKEN_FIELD,
                refreshToken);

        if (consumed == null || consumed == 0L) {
            throw InvalidRefreshTokenException.EXCEPTION;
        }
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
