package com.storix.infrastructure.external.portone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.infrastructure.external.portone.exception.decoder.PortOneErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

// 컴포넌트 스캔에 걸리면 모든 Feign 클라이언트의 기본 ErrorDecoder 가 된다
// @FeignClient(configuration = ...) 로만 로딩되도록 어노테이션을 붙이지 않는다
public class PortOneClientConfig {

    @Bean
    public ErrorDecoder portOneErrorDecoder(ObjectMapper objectMapper) {
        return new PortOneErrorDecoder(objectMapper);
    }
}
