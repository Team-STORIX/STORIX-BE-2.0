package com.storix.api.domain.aichat.helper;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] 답변 지표")
class AiChatMetricsHelperTest {

    @Test
    @DisplayName("모델 이름이 없으면 unknown 으로 남기고 예외를 던지지 않는다")
    void unknownModel() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AiChatMetricsHelper helper = new AiChatMetricsHelper(registry, "server");

        helper.recordUsage(null, 10, 5, 0);
        helper.recordUsage(" ", 1, 1, 0);

        assertThat(registry.get("ai.chat.tokens").tags("model", "unknown", "type", "input").counter().count()).isEqualTo(11);
    }
}
