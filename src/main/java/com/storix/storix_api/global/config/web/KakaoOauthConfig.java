package com.storix.storix_api.global.config.web;

import com.storix.storix_api.global.apiPayload.exception.web.KakaoOauthErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoOauthConfig {

    @Bean
    public ErrorDecoder kakaoOauthErrorDecoder() {
        return new KakaoOauthErrorDecoder();
    }
}
