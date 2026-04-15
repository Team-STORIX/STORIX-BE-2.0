package com.storix.common.property;

import lombok.Getter;

@Getter
public class KakaoProperties {

    private final String baseUri;
    private final String clientId;       // REST API Key (웹 플로우 OIDC aud)
    private final String clientSecret;
    private final String adminKey;
    private final String nativeAppKey;   // Native App Key (iOS/Android SDK 플로우 OIDC aud)

    public KakaoProperties(String baseUri, String clientId, String clientSecret, String adminKey, String nativeAppKey) {
        this.baseUri = baseUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminKey = adminKey;
        this.nativeAppKey = nativeAppKey;
    }

}
