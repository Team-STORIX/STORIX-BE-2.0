package com.storix.common.property;

import lombok.Getter;

@Getter
public class XProperties {

    private final String clientId;
    private final String clientSecret;

    public XProperties(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

}
