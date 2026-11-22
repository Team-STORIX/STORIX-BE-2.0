package com.storix.api.domain.aichat.helper;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.aichat.exception.AiChatUsageLimitExceededException;
import com.storix.infrastructure.global.ratelimit.RateLimitCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class AiChatUsageLimitHelper {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final long COUNTER_TTL_SECONDS = 2 * 24 * 60 * 60;

    private final RateLimitCounter rateLimitCounter;
    private final int dailyLimit;

    public AiChatUsageLimitHelper(RateLimitCounter rateLimitCounter, @Value("${ai.chat.daily-limit}") int dailyLimit) {
        this.rateLimitCounter = rateLimitCounter;
        this.dailyLimit = dailyLimit;
    }

    // 0 이하면 끔. 켜면 한국 날짜 기준 하루 질문 수를 센다. Redis 장애면 막지 않고 통과
    public void check(Long userId) {
        if (dailyLimit <= 0) {
            return;
        }
        String key = RedisKeyStatic.AiChat.USAGE_PREFIX + userId + ":" + LocalDate.now(ZONE).format(DateTimeFormatter.BASIC_ISO_DATE);
        if (rateLimitCounter.increment(key, COUNTER_TTL_SECONDS) > dailyLimit) {
            throw AiChatUsageLimitExceededException.EXCEPTION;
        }
    }
}
