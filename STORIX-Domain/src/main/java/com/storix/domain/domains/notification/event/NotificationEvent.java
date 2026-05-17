package com.storix.domain.domains.notification.event;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.domain.TargetType;

// 알림 발송 트리거 이벤트.
// 발행 지점(Service) 은 정적 팩토리로 만들어 publisher.publish() 만 호출.
// AFTER_COMMIT 시점에 NotificationEventListener 가 인앱 저장 + FCM 발송 수행.
public record NotificationEvent(
        Long recipientUserId,
        NotificationType notificationType,
        TargetType targetType,
        Long targetId,
        Long parentTargetId,
        String title,
        String content
) {

    /** 좋아요 */
    // 1. 내 피드에 좋아요
    public static NotificationEvent likeFeed(Long recipientUserId, Long feedId, String actorNickname) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.LIKE_FEED,
                TargetType.FEED,
                feedId,
                null,
                STORIXStatic.Notification.TITLE_FEED,
                String.format(STORIXStatic.Notification.TPL_LIKE_FEED, actorNickname)
        );
    }

    // 2. 내 리뷰에 좋아요
    public static NotificationEvent likeReview(Long recipientUserId, Long reviewId, String actorNickname) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.LIKE_REVIEW,
                TargetType.REVIEW,
                reviewId,
                null,
                STORIXStatic.Notification.TITLE_REVIEW,
                String.format(STORIXStatic.Notification.TPL_LIKE_REVIEW, actorNickname)
        );
    }

    // 3. 내 피드 댓글에 좋아요 — parentTargetId 로 부모 피드 ID 전달
    public static NotificationEvent likeComment(Long recipientUserId, Long commentId, Long feedId,
                                                String actorNickname) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.LIKE_COMMENT,
                TargetType.COMMENT,
                commentId,
                feedId,
                STORIXStatic.Notification.TITLE_FEED,
                String.format(STORIXStatic.Notification.TPL_LIKE_COMMENT, actorNickname)
        );
    }


    /** 댓글 / 답댓글 */
    // 1. 내 피드에 댓글
    public static NotificationEvent commentOnFeed(Long recipientUserId, Long feedId,
                                                  String actorNickname, String commentContent) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.COMMENT_ON_FEED,
                TargetType.FEED,
                feedId,
                null,
                STORIXStatic.Notification.TITLE_FEED,
                String.format(STORIXStatic.Notification.TPL_COMMENT_ON_FEED, actorNickname, preview(commentContent))
        );
    }

    // 2. 내 댓글에 답댓글 — parentTargetId 로 부모 피드 ID 전달
    public static NotificationEvent replyOnComment(Long recipientUserId, Long parentCommentId, Long feedId,
                                                   String actorNickname, String replyContent) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.REPLY_ON_COMMENT,
                TargetType.COMMENT,
                parentCommentId,
                feedId,
                STORIXStatic.Notification.TITLE_FEED,
                String.format(STORIXStatic.Notification.TPL_REPLY_ON_COMMENT, actorNickname, preview(replyContent))
        );
    }


    /** 선정 알림 */
    // 1. 내 피드가 오늘의 피드로 선정
    public static NotificationEvent todayFeed(Long recipientUserId, Long feedId) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.TODAY_FEED,
                TargetType.FEED,
                feedId,
                null,
                STORIXStatic.Notification.TITLE_FEED,
                STORIXStatic.Notification.TPL_TODAY_FEED
        );
    }

    // 2. 내가 참여한 토픽룸이 HOT 으로 선정
    public static NotificationEvent hotTopicRoom(Long recipientUserId, Long topicRoomId, String topicRoomTitle) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.HOT_TOPIC_ROOM,
                TargetType.TOPIC_ROOM,
                topicRoomId,
                null,
                STORIXStatic.Notification.TITLE_TOPIC_ROOM,
                String.format(STORIXStatic.Notification.TPL_HOT_TOPIC_ROOM, topicRoomTitle)
        );
    }


    /** 마케팅/광고 (운영자 발송) */
    // 운영자가 입력한 타이틀/본문에 고정 prefix '(광고) ' + suffix '(수신거부 : 설정)' 자동 부착
    public static NotificationEvent marketing(Long recipientUserId, String adTitle, String adBody) {
        return new NotificationEvent(
                recipientUserId,
                NotificationType.MARKETING,
                TargetType.NONE,
                null,
                null,
                String.format(STORIXStatic.Notification.TITLE_MARKETING, adTitle),
                String.format(STORIXStatic.Notification.TPL_MARKETING, adBody)
        );
    }


    /** 운영 정책 */
    // 1. 신고 접수 완료
    public static NotificationEvent reportReceived(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.REPORT_RECEIVED,
                STORIXStatic.Notification.TITLE_REPORT_RECEIVED,
                STORIXStatic.Notification.TPL_REPORT_RECEIVED);
    }

    // 2. 신고 처리 완료
    public static NotificationEvent reportProcessed(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.REPORT_PROCESSED,
                STORIXStatic.Notification.TITLE_REPORT_PROCESSED,
                STORIXStatic.Notification.TPL_REPORT_PROCESSED);
    }

    // 3. 서비스 이용 제한 (7일)
    public static NotificationEvent restriction7d(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.RESTRICTION_7D,
                STORIXStatic.Notification.TITLE_RESTRICTION,
                STORIXStatic.Notification.TPL_RESTRICTION_7D);
    }

    // 4. 서비스 이용 제한 (30일)
    public static NotificationEvent restriction30d(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.RESTRICTION_30D,
                STORIXStatic.Notification.TITLE_RESTRICTION,
                STORIXStatic.Notification.TPL_RESTRICTION_30D);
    }

    // 5. 서비스 이용약관 업데이트
    public static NotificationEvent tosUpdate(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.TOS_UPDATE,
                STORIXStatic.Notification.TITLE_TOS_UPDATE,
                STORIXStatic.Notification.TPL_TOS_UPDATE);
    }

    // 6. 개인정보 처리방침 업데이트
    public static NotificationEvent privacyUpdate(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.PRIVACY_UPDATE,
                STORIXStatic.Notification.TITLE_PRIVACY_UPDATE,
                STORIXStatic.Notification.TPL_PRIVACY_UPDATE);
    }

    // 7. 신기능 업데이트
    public static NotificationEvent featureUpdate(Long recipientUserId) {
        return policyEvent(recipientUserId, NotificationType.FEATURE_UPDATE,
                STORIXStatic.Notification.TITLE_FEATURE_UPDATE,
                STORIXStatic.Notification.TPL_FEATURE_UPDATE);
    }

    // 운영 정책 알림 공통 빌더 (타겟 없음, 고정 title/body)
    private static NotificationEvent policyEvent(Long recipientUserId, NotificationType type,
                                                 String title, String body) {
        return new NotificationEvent(recipientUserId, type, TargetType.NONE, null, null, title, body);
    }


    // 본문 미리보기 — 10자 초과 시 잘라서 '…' 부가
    private static String preview(String text) {
        if (text == null) return "";
        int max = STORIXStatic.Notification.CONTENT_PREVIEW_MAX;
        return text.length() > max
                ? text.substring(0, max) + STORIXStatic.Notification.CONTENT_PREVIEW_SUFFIX
                : text;
    }
}
