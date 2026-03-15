package com.storix.storix_api.global.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.storix.storix_api")
public class FeignClientConfig {

    @Bean
    public Encoder formEncoder() { return new FormEncoder(); }
}
