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

    // refreshToken:{userId} 해시에 발급된 토큰을 필드로, 만료 시각(epoch second)을 값으로 담는다.
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";
    private static final int MAX_DEVICE_COUNT = 5;

    // 만료분을 걷어내고 상한을 넘기면 오래된 순으로 밀어낸 뒤 새 토큰을 넣는다.
    private static final RedisScript<Long> SAVE_SCRIPT = new DefaultRedisScript<>("""
            local now = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])
            local limit = tonumber(ARGV[4])

            local alive = {}
            local entries = redis.call('HGETALL', KEYS[1])
            for i = 1, #entries, 2 do
                local field = entries[i]
                local exp = tonumber(entries[i + 1])
                if exp == nil or exp <= now then
                    redis.call('HDEL', KEYS[1], field)
                else
                    table.insert(alive, { field = field, exp = exp })
                end
            end

            table.sort(alive, function(a, b) return a.exp < b.exp end)
            for i = 1, #alive - limit + 1 do
                redis.call('HDEL', KEYS[1], alive[i].field)
            end

            redis.call('HSET', KEYS[1], ARGV[1], now + ttl)
            redis.call('EXPIRE', KEYS[1], ttl)
            return redis.call('HLEN', KEYS[1])
            """, Long.class);

    // 살아있는 토큰일 때만 삭제하고 1을 반환. 동시 요청 중 하나만 1을 받는다.
    private static final RedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            local exp = redis.call('HGET', KEYS[1], ARGV[1])
            if exp == false or tonumber(exp) <= tonumber(ARGV[2]) then
                return 0
            end
            return redis.call('HDEL', KEYS[1], ARGV[1])
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

    public void consumeRefreshToken(Long userId, String refreshToken) {
        Long consumed = redisTemplate.execute(CONSUME_SCRIPT,
                Collections.singletonList(refreshTokenKey(userId)),
                refreshToken,
                String.valueOf(now()));

        if (consumed == null || consumed == 0L) {
            throw InvalidRefreshTokenException.EXCEPTION;
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
