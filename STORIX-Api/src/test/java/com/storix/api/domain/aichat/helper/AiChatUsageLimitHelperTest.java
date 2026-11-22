package com.storix.api.domain.aichat.helper;

import com.storix.domain.domains.aichat.exception.AiChatUsageLimitExceededException;
import com.storix.infrastructure.global.ratelimit.RateLimitCounter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("[AI 채팅] 하루 사용 횟수 제한")
class AiChatUsageLimitHelperTest {

    @Mock
    private RateLimitCounter rateLimitCounter;

    @Test
    @DisplayName("상한이 0 이면 세지도 막지도 않는다")
    void disabled() {
        AiChatUsageLimitHelper helper = new AiChatUsageLimitHelper(rateLimitCounter, 0);

        assertThatCode(() -> helper.check(1L)).doesNotThrowAnyException();
        helper.record(1L);
        verifyNoInteractions(rateLimitCounter);
    }

    @Test
    @DisplayName("켜면 오늘 쓴 횟수가 상한에 닿은 뒤부터 막는다")
    void limitExceeded() {
        AiChatUsageLimitHelper helper = new AiChatUsageLimitHelper(rateLimitCounter, 2);
        given(rateLimitCounter.current(startsWith("ai-chat:usage:1:"))).willReturn(1L, 2L);

        assertThatCode(() -> helper.check(1L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> helper.check(1L)).isSameAs(AiChatUsageLimitExceededException.EXCEPTION);
    }

    @Test
    @DisplayName("record 는 사용자 · 날짜 키로 1 을 더한다")
    void record() {
        AiChatUsageLimitHelper helper = new AiChatUsageLimitHelper(rateLimitCounter, 2);

        helper.record(1L);

        verify(rateLimitCounter).increment(startsWith("ai-chat:usage:1:"), anyLong());
    }

    @Test
    @DisplayName("Redis 장애로 0 이 오면 통과")
    void passWhenCounterFails() {
        AiChatUsageLimitHelper helper = new AiChatUsageLimitHelper(rateLimitCounter, 2);
        given(rateLimitCounter.current(anyString())).willReturn(0L);

        assertThatCode(() -> helper.check(1L)).doesNotThrowAnyException();
    }
}
