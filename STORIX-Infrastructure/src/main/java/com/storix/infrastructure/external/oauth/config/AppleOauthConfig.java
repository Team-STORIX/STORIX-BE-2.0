package com.storix.infrastructure.external.oauth.config;

import com.storix.infrastructure.external.oauth.exception.decoder.AppleOauthErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppleOauthConfig {

    @Bean
    public ErrorDecoder appleOauthErrorDecoder() {
        return new AppleOauthErrorDecoder();
    }
}
