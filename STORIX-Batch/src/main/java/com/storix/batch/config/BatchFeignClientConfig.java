package com.storix.batch.config;

import com.storix.infrastructure.external.portone.client.PortOneIdentityVerificationClient;
import feign.Request;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients(clients = PortOneIdentityVerificationClient.class)
public class BatchFeignClientConfig {

    private static final int CONNECT_TIMEOUT_MS = 3_000;
    private static final int READ_TIMEOUT_MS = 5_000;

    // FeignClientConfig 를 batch 스캔에서 빼서 같은 값을 둔다
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                READ_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                true);
    }
}
