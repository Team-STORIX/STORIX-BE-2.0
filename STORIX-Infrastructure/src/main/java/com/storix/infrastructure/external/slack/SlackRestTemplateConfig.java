package com.storix.infrastructure.external.slack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SlackRestTemplateConfig {

    @Bean
    public RestTemplate slackRestTemplate() {
        return new RestTemplate();
    }
}
