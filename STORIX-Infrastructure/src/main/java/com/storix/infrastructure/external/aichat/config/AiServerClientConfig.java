package com.storix.infrastructure.external.aichat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.utils.STORIXStatic;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.concurrent.TimeUnit;

public class AiServerClientConfig {

    private static final int CONNECT_TIMEOUT_MS = 2_000;
    private static final int READ_TIMEOUT_MS = 35_000;

    // @Configuration 을 붙이지 않음. 스캔에 걸리면 모든 Feign 클라이언트 기본값이 됨
    // 전역 FormEncoder 는 JSON 본문을 못 보내서 따로 둠
    @Bean
    public Encoder aiServerEncoder(ObjectMapper objectMapper) {
        HttpMessageConverters converters = new HttpMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
        return new SpringEncoder(() -> converters);
    }

    // AI 서버는 조용할 때 15초마다 heartbeat 를 보낸다. 두 번 넘게 아무것도 안 오면 끊긴 것으로 본다
    @Bean
    public Request.Options aiServerRequestOptions() {
        return new Request.Options(
                CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                READ_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                true);
    }

    @Bean
    public RequestInterceptor aiServerTokenInterceptor(@Value("${internal.ai-token}") String aiToken) {
        return template -> template.header(STORIXStatic.Internal.AI_TOKEN_HEADER, aiToken);
    }
}
