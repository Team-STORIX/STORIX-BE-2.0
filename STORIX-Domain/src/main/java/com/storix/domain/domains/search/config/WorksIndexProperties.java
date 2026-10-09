package com.storix.domain.domains.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorksIndexProperties {

    private final String alias;

    public WorksIndexProperties(@Value("${spring.profiles.active}") String profile) {
        this.alias = "works-" + profile;
    }

    public String alias() {
        return alias;
    }
}
