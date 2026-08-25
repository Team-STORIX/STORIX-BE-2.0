package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {

    // 공개값
    private final String storeId;
    private final String channelKey;

    // 비공개값
    private final String apiSecret;

    public PortOneProperties(String storeId, String channelKey, String apiSecret) {
        this.storeId = storeId;
        this.channelKey = channelKey;
        this.apiSecret = apiSecret;
    }
}
