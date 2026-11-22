package com.storix.infrastructure.external.aichat.client;

import com.storix.infrastructure.external.aichat.config.AiServerClientConfig;
import com.storix.infrastructure.external.aichat.dto.AiServerSearchRequest;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "AiServerClient",
        url = "${ai.server.base-url}",
        configuration = AiServerClientConfig.class
)
public interface AiServerClient {

    // 반환 타입이 Response 라 401 · 500 도 예외 없이 돌려줌. 받는 쪽이 닫음
    @PostMapping(
            value = "/internal/search/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    Response searchStream(@RequestBody AiServerSearchRequest request);
}
