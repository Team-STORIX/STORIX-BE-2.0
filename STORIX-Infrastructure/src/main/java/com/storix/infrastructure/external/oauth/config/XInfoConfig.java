package com.storix.infrastructure.external.oauth.config;

import com.storix.infrastructure.external.oauth.exception.decoder.XOauthErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XInfoConfig {

    @Bean
    public ErrorDecoder xInfoErrorDecoder() {
        return new XOauthErrorDecoder();
    }
}
