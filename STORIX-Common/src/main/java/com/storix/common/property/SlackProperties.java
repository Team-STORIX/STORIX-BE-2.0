package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

    private final String botToken;
    private final String channelId;
    private final String signingSecret;

    public SlackProperties(String botToken, String channelId, String signingSecret) {
        this.botToken = botToken;
        this.channelId = channelId;
        this.signingSecret = signingSecret;
    }
}
