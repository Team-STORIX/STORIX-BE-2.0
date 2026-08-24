package com.storix.domain.domains.adultverification.domain;

import java.util.List;

public enum AdultVerificationStatus {

    PENDING,

    VERIFIED,

    FAILED,

    EXPIRED,

    // 관리자 해제용. 진입점은 아직 없다
    REVOKED;

    // 아직 확정되지 않은 상태. 포트원이 같은 건의 재시도를 허용해 FAILED 도 넘어올 수 있다
    public static final List<AdultVerificationStatus> CONFIRMABLE = List.of(PENDING, FAILED);

    public boolean isConfirmable() {
        return CONFIRMABLE.contains(this);
    }
}
