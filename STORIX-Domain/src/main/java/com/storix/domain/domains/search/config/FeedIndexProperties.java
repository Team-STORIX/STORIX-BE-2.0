package com.storix.domain.domains.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeedIndexProperties {

    private final String alias;

    public FeedIndexProperties(@Value("${spring.profiles.active}") String profile) {
        this.alias = "feeds-" + profile;
    }

    public String alias() {
        return alias;
    }
}
