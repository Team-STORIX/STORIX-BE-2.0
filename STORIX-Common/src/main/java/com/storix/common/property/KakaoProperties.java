package com.storix.common.property;

import lombok.Getter;

@Getter
public class KakaoProperties {

    private final String baseUri;
    private final String clientId;
    private final String clientSecret;
    private final String adminKey;

    public KakaoProperties(String baseUri, String clientId, String clientSecret, String adminKey) {
        this.baseUri = baseUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminKey = adminKey;
    }

}
