package com.storix.infrastructure.external.oauth.config;

import com.storix.infrastructure.external.oauth.exception.decoder.KakaoInfoErrorDecoder;
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
