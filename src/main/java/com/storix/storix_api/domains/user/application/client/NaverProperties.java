package com.storix.storix_api.domains.user.application.client;

import lombok.Getter;

@Getter
public class NaverProperties {

    private final String baseUri;
    private final String clientId;
    private final String clientSecret;

    public NaverProperties(String baseUri, String clientId, String clientSecret) {
        this.baseUri = baseUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

}