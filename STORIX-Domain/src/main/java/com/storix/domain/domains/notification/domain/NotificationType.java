package com.storix.domain.domains.notification.domain;

/**
 * 알림 타입
 * - '마케팅/광고': 마케팅 수신 동의가 있어야 발송 가능
 * */
public enum NotificationType {

    /* ─────────── 서비스 ─────────── */
    LIKE_FEED,         // 내 피드에 좋아요
    LIKE_REVIEW,       // 내 리뷰에 좋아요
    LIKE_COMMENT,      // 내 피드 댓글에 좋아요
    COMMENT_ON_FEED,   // 내 피드에 댓글
    REPLY_ON_COMMENT,  // 내 댓글에 답댓글

    TODAY_FEED,        // 내 피드가 오늘의 피드로 선정
    HOT_TOPIC_ROOM,    // 내가 참여한 토픽룸이 HOT으로 선정

    /* ─────────── 마케팅/광고 ─────────── */
    MARKETING,         // 운영자 발송 이벤트/광고

    /* ─────────── 운영 정책 ─────────── */
    REPORT_RECEIVED,   // 신고 접수 완료
    REPORT_PROCESSED,  // 신고 처리 완료
    RESTRICTION_7D,    // 서비스 이용 제한 안내 (7일)
    RESTRICTION_30D,   // 서비스 이용 제한 안내 (30일)
    TOS_UPDATE,        // 서비스 이용약관 업데이트
    PRIVACY_UPDATE,    // 개인정보 처리방침 업데이트
    FEATURE_UPDATE;    // 신기능 업데이트


    // 일시 정지(SUSPENDED) 유저에게도 푸시를 발송할지 여부 — 서비스 이용 제한 안내만
    public boolean deliverableToSuspendedUser() {
        return switch (this) {
            case RESTRICTION_7D, RESTRICTION_30D -> true;
            default -> false;
        };
    }

    // FE 아이콘 분기용 카테고리 (13개 type -> 6개 category)
    public NotificationCategory category() {
        return switch (this) {
            case LIKE_FEED, LIKE_COMMENT, COMMENT_ON_FEED, REPLY_ON_COMMENT, TODAY_FEED
                    -> NotificationCategory.FEED;
            case LIKE_REVIEW
                    -> NotificationCategory.REVIEW;
            case HOT_TOPIC_ROOM
                    -> NotificationCategory.TOPIC_ROOM;
            case MARKETING
                    -> NotificationCategory.MARKETING;
            case REPORT_RECEIVED, REPORT_PROCESSED, RESTRICTION_7D, RESTRICTION_30D
                    -> NotificationCategory.REPORT;
            case TOS_UPDATE, PRIVACY_UPDATE, FEATURE_UPDATE
                    -> NotificationCategory.POLICY;
        };
    }

}
