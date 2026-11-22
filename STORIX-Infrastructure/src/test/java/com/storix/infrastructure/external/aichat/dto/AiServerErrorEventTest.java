package com.storix.infrastructure.external.aichat.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[AI 채팅] AI 서버 오류 코드 변환")
class AiServerErrorEventTest {

    @Test
    @DisplayName("앱 코드는 그대로, 모르는 코드와 null 은 INTERNAL")
    void appCode() {
        assertThat(new AiServerErrorEvent("TIMEOUT", "m").appCode()).isEqualTo("TIMEOUT");
        assertThat(new AiServerErrorEvent("SEARCH_UNAVAILABLE", "m").appCode()).isEqualTo("SEARCH_UNAVAILABLE");
        assertThat(new AiServerErrorEvent("INVALID_REQUEST", "m").appCode()).isEqualTo("INTERNAL");
        assertThat(new AiServerErrorEvent(null, null).appCode()).isEqualTo("INTERNAL");
    }
}
