package com.storix.infrastructure.config;

import com.storix.infrastructure.external.oauth.client.KakaoInfoClient;
import com.storix.infrastructure.external.oauth.client.KakaoOAuthClient;
import com.storix.infrastructure.external.oauth.client.NaverInfoClient;
import com.storix.infrastructure.external.oauth.client.NaverOAuthClient;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {
        KakaoInfoClient.class,
        KakaoOAuthClient.class,
        NaverInfoClient.class,
        NaverOAuthClient.class
})
public class FeignClientConfig {

    @Bean
    public Encoder formEncoder() { return new FormEncoder(); }
}
