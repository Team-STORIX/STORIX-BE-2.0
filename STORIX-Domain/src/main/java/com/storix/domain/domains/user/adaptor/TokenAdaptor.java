package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.OnboardingToken;
import com.storix.domain.domains.user.dto.OnboardingPrincipal;
import com.storix.domain.domains.user.repository.OnboardingTokenRepository;
import com.storix.domain.domains.user.exception.auth.InvalidLogoutException;
import com.storix.domain.domains.user.exception.token.InvalidRefreshTokenException;
import com.storix.domain.domains.user.exception.token.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenAdaptor {

    private final OnboardingTokenRepository onboardingTokenRepository;
    private final StringRedisTemplate redisTemplate;

    // refreshToken:{userId} 해시에 발급된 토큰을 필드로, 발급 시각(epoch second)을 값으로 담는다.
    // 만료는 필드별 TTL(HEXPIRE)에 맡기므로 만료된 토큰은 조회 시점에 이미 사라져 있다.
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";
    private static final int MAX_DEVICE_COUNT = 5;

    // 기기 상한을 넘기면 오래된 순으로 밀어낸 뒤 새 토큰을 넣는다.
    private static final RedisScript<Long> SAVE_SCRIPT = new DefaultRedisScript<>("""
            local issuedAt = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])
            local limit = tonumber(ARGV[4])

            local issued = {}
            local entries = redis.call('HGETALL', KEYS[1])
            for i = 1, #entries, 2 do
                table.insert(issued, { field = entries[i], at = tonumber(entries[i + 1]) })
            end

            table.sort(issued, function(a, b) return a.at < b.at end)
            for i = 1, #issued - limit + 1 do
                redis.call('HDEL', KEYS[1], issued[i].field)
            end

            redis.call('HSET', KEYS[1], ARGV[1], issuedAt)
            redis.call('HEXPIRE', KEYS[1], ttl, 'FIELDS', 1, ARGV[1])
            return redis.call('HLEN', KEYS[1])
            """, Long.class);

    // RefreshToken
    public void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        redisTemplate.execute(SAVE_SCRIPT,
                Collections.singletonList(refreshTokenKey(userId)),
                refreshToken,
                String.valueOf(now()),
                String.valueOf(ttlSeconds),
                String.valueOf(MAX_DEVICE_COUNT));
    }

    // HDEL 단일 명령이 원자적이라 동시 요청 중 하나만 1을 받는다.
    public void consumeRefreshToken(Long userId, String refreshToken) {
        Long consumed = redisTemplate.opsForHash().delete(refreshTokenKey(userId), refreshToken);
        if (consumed == null || consumed == 0L) {
            throw InvalidRefreshTokenException.EXCEPTION;
        }
    }

    public void deleteRefreshToken(Long userId, String refreshToken) {
        Long deleted = redisTemplate.opsForHash().delete(refreshTokenKey(userId), refreshToken);
        if (deleted == null || deleted == 0L) {
            throw InvalidLogoutException.EXCEPTION;
        }
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        if (Boolean.FALSE.equals(redisTemplate.delete(refreshTokenKey(userId)))) {
            throw InvalidLogoutException.EXCEPTION;
        }
    }

    // 토큰이 없어도 예외 없이 삭제 (계정 정지 시 사용)
    public void deleteRefreshTokenByUserIdIfPresent(Long userId) {
        redisTemplate.delete(refreshTokenKey(userId));
    }

    private String refreshTokenKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }

    private long now() {
        return Instant.now().getEpochSecond();
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
