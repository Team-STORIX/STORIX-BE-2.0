package com.storix.common.property;

import lombok.Getter;

@Getter
public class AppleProperties {

    private final String baseUri;
    private final String clientId;
    private final String teamId;
    private final String keyId;
    private final String privateKey;

    public AppleProperties(String baseUri, String clientId, String teamId, String keyId, String privateKey) {
        this.baseUri = baseUri;
        this.clientId = clientId;
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKey = privateKey;
    }

}
