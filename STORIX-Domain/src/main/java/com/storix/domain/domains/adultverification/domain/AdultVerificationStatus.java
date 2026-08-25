package com.storix.domain.domains.adultverification.domain;

import java.util.List;

public enum AdultVerificationStatus {

    PENDING,

    VERIFIED,

    ABANDONED, // 배치 정리

    EXPIRED,

    // 관리자 해제용. 진입점은 아직 없다
    REVOKED;

    // 아직 확정되지 않은 건. 방치로 정리됐어도 사용자가 인증을 마쳤다면 확정할 수 있다
    public static final List<AdultVerificationStatus> CONFIRMABLE = List.of(PENDING, ABANDONED);

    public boolean isConfirmable() {
        return CONFIRMABLE.contains(this);
    }
}
