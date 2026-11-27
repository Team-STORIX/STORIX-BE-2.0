package com.storix.domain.domains.works.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorksCreationPolicy {

    private static final String DEV_PROFILE = "dev";

    private final boolean blocked;

    // dev 작품은 prod 동기화로만 채움 (같은 작품이 dev ID 로 따로 생기지 않게)
    public WorksCreationPolicy(@Value("${spring.profiles.active}") String profile) {
        this.blocked = DEV_PROFILE.equals(profile);
    }

    public boolean blocked() {
        return blocked;
    }
}
