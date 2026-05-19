package com.storix.domain.domains.notification.domain;

// 알림이 가리키는 타겟의 종류. 알림 탭 시 FE 라우팅 분기 + targetId 의 해석 기준.
public enum TargetType {
    FEED,
    REVIEW,
    COMMENT,
    TOPIC_ROOM,
    NONE         // 운영자/약관 등 타겟 없는 알림
}
