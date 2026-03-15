package com.storix.infrastructure.external.oauth.config;

import com.storix.infrastructure.external.oauth.exception.decoder.KakaoOauthErrorDecoder;
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
