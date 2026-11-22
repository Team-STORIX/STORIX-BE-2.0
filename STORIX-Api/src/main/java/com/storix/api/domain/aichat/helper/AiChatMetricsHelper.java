package com.storix.api.domain.aichat.helper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class AiChatMetricsHelper {

    public static final String NO_ERROR = "none";
    public static final String UNKNOWN_MODEL = "unknown";

    private static final String METRIC_ANSWER = "ai.chat.answer";
    private static final String METRIC_FIRST_TOKEN = "ai.chat.first.token";
    private static final String METRIC_TOKENS = "ai.chat.tokens";
    private static final Duration[] SLO = {
            Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(5),
            Duration.ofSeconds(10), Duration.ofSeconds(20), Duration.ofSeconds(40)
    };

    private final MeterRegistry meterRegistry;
    private final String responder;

    public AiChatMetricsHelper(MeterRegistry meterRegistry, @Value("${ai.chat.responder}") String responder) {
        this.meterRegistry = meterRegistry;
        this.responder = responder;
    }

    // 질문 받은 때부터 done · error 까지. status 는 답변 상태, 오류면 ERROR 와 오류 코드
    public void recordAnswer(String status, String errorCode, long elapsedNanos) {
        Timer.builder(METRIC_ANSWER)
                .tag("responder", responder)
                .tag("status", status)
                .tag("error", errorCode)
                .serviceLevelObjectives(SLO)
                .register(meterRegistry)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordFirstToken(long elapsedNanos) {
        Timer.builder(METRIC_FIRST_TOKEN)
                .tag("responder", responder)
                .serviceLevelObjectives(SLO)
                .register(meterRegistry)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    // 비용은 대시보드에서 모델별 단가를 곱해 계산. 모델 이름이 없어도 답변이 오류로 바뀌지 않게 unknown 으로
    public void recordUsage(String model, int inputTokens, int outputTokens, int cacheReadTokens) {
        model = model == null || model.isBlank() ? UNKNOWN_MODEL : model;
        meterRegistry.counter(METRIC_TOKENS, "model", model, "type", "input").increment(inputTokens);
        meterRegistry.counter(METRIC_TOKENS, "model", model, "type", "output").increment(outputTokens);
        meterRegistry.counter(METRIC_TOKENS, "model", model, "type", "cache_read").increment(cacheReadTokens);
    }
}
