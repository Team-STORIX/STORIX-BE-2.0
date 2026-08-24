package com.storix.infrastructure.config;

import com.storix.infrastructure.external.oauth.client.AppleOAuthClient;
import com.storix.infrastructure.external.oauth.client.KakaoInfoClient;
import com.storix.infrastructure.external.oauth.client.KakaoOAuthClient;
import com.storix.infrastructure.external.oauth.client.NaverInfoClient;
import com.storix.infrastructure.external.oauth.client.NaverOAuthClient;
import com.storix.infrastructure.external.oauth.client.XInfoClient;
import com.storix.infrastructure.external.oauth.client.XOAuthClient;
import feign.Request;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients(clients = {
        KakaoInfoClient.class,
        KakaoOAuthClient.class,
        NaverInfoClient.class,
        NaverOAuthClient.class,
        AppleOAuthClient.class,
        XOAuthClient.class,
        XInfoClient.class
})
public class FeignClientConfig {

    // 기본값은 connect 10초 / read 60초다. 외부가 느릴 때 요청 스레드를 그만큼 붙잡는다
    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int READ_TIMEOUT_MS = 5_000;

    @Bean
    public Encoder formEncoder() { return new FormEncoder(); }

    // 클라이언트별로 다르게 두려면 각 @FeignClient 의 configuration 에서 덮어쓰면 된다
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                READ_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                true);
    }
}
