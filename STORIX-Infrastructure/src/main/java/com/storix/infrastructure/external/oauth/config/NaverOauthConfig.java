package com.storix.infrastructure.external.oauth.config;

import com.storix.infrastructure.external.oauth.exception.decoder.NaverOauthErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverOauthConfig {

    @Bean
    public ErrorDecoder naverOauthErrorDecoder() {
        return new NaverOauthErrorDecoder();
    }
}
