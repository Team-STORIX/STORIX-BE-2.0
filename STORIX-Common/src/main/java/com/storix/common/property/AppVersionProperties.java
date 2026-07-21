package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app-version")
public class AppVersionProperties {

    private final Platform ios;
    private final Platform android;

    public AppVersionProperties(Platform ios, Platform android) {
        this.ios = ios;
        this.android = android;
    }

    @Getter
    public static class Platform {
        private final String minSupported;
        private final String latest;

        public Platform(String minSupported, String latest) {
            this.minSupported = minSupported;
            this.latest = latest;
        }
    }
}
