package com.storix.storix_api.global.config.web;

import com.storix.storix_api.global.apiPayload.exception.web.KakaoInfoErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoInfoConfig {

    @Bean
    public ErrorDecoder kakaoInfoErrorDecoder() {
        return new KakaoInfoErrorDecoder();
    }
}
