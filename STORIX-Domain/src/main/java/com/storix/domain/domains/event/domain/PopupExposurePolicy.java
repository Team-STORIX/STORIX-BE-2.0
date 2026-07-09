package com.storix.domain.domains.event.domain;

public enum PopupExposurePolicy {
    ALWAYS_DURING_PERIOD, // 닫기 버튼만 - 기간 내 항상 노출
    ONCE_PER_DAY          // '오늘 다시 보지 않기' - 유저당 하루 1회
}
