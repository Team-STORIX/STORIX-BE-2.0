package com.storix.infrastructure.global.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("[요청 제한] 횟수 집계")
class RateLimitCounterTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final RateLimitCounter counter = new RateLimitCounter(redisTemplate);

    @Test
    @DisplayName("Redis 장애면 0 을 돌려줘 요청을 막지 않는다")
    @SuppressWarnings("unchecked")
    void failOpen() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenThrow(new RedisConnectionFailureException("down"));

        assertThat(counter.increment("rate-limit:DEFAULT:ip:1.1.1.1:1", 60)).isZero();
    }
}
