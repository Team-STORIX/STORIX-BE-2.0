package com.storix.domain.domains.works.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorksSyncPolicy {

    private static final String DEV_PROFILE = "dev";

    private final boolean syncedFromProd;

    // dev 작품은 매일 prod 동기화로만 채움 (Hub storix-works-dev-sync)
    public WorksSyncPolicy(@Value("${spring.profiles.active}") String profile) {
        this.syncedFromProd = DEV_PROFILE.equals(profile);
    }

    // 같은 작품이 dev ID 로 따로 생기지 않게
    public boolean blocksCreation() {
        return syncedFromProd;
    }

    // 꺼져 있는 동안 동기화된 작품을 검색에 반영
    public boolean reindexOnStartup() {
        return syncedFromProd;
    }
}
