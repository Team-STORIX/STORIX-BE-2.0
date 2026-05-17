package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private final String projectId;
    private final String credentialsBase64;

    public FirebaseProperties(String projectId, String credentialsBase64) {
        this.projectId = projectId;
        this.credentialsBase64 = credentialsBase64;
    }
}
