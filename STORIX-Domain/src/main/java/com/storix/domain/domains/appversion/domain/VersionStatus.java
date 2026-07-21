package com.storix.domain.domains.appversion.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VersionStatus {

    LATEST("최신 버전"),
    UPDATE_AVAILABLE("업데이트 권장"),
    UPDATE_REQUIRED("업데이트 필요");

    private final String description;
}
