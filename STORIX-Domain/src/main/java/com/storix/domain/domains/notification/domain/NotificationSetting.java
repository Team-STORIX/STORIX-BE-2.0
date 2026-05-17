package com.storix.domain.domains.notification.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notification_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    // 서비스 알림 수신 여부
    @Column(name = "like_feed_enabled", nullable = false)
    private boolean likeFeedEnabled;

    @Column(name = "like_review_enabled", nullable = false)
    private boolean likeReviewEnabled;

    @Column(name = "like_comment_enabled", nullable = false)
    private boolean likeCommentEnabled;

    @Column(name = "comment_on_feed_enabled", nullable = false)
    private boolean commentOnFeedEnabled;

    @Column(name = "reply_on_comment_enabled", nullable = false)
    private boolean replyOnCommentEnabled;

    @Column(name = "today_feed_enabled", nullable = false)
    private boolean todayFeedEnabled;

    @Column(name = "hot_topic_room_enabled", nullable = false)
    private boolean hotTopicRoomEnabled;

    // 마케팅/광고 수신 여부
    @Column(name = "marketing_enabled", nullable = false)
    private boolean marketingEnabled;


    /** 생성자 메서드 */
    // 신규 유저용 기본값 — 마케팅만 OFF, 나머지 ON
    public static NotificationSetting defaultFor(Long userId) {
        NotificationSetting p = new NotificationSetting();
        p.userId = userId;
        p.likeFeedEnabled = true;
        p.likeReviewEnabled = true;
        p.likeCommentEnabled = true;
        p.commentOnFeedEnabled = true;
        p.replyOnCommentEnabled = true;
        p.todayFeedEnabled = true;
        p.hotTopicRoomEnabled = true;
        p.marketingEnabled = false;
        return p;
    }


    /** 비즈니스 메서드 */
    // 부분 갱신 — null 인자는 변경하지 않음
    public void update(Boolean likeFeedEnabled,
                       Boolean likeReviewEnabled,
                       Boolean likeCommentEnabled,
                       Boolean commentOnFeedEnabled,
                       Boolean replyOnCommentEnabled,
                       Boolean todayFeedEnabled,
                       Boolean hotTopicRoomEnabled,
                       Boolean marketingEnabled) {
        if (likeFeedEnabled != null) this.likeFeedEnabled = likeFeedEnabled;
        if (likeReviewEnabled != null) this.likeReviewEnabled = likeReviewEnabled;
        if (likeCommentEnabled != null) this.likeCommentEnabled = likeCommentEnabled;
        if (commentOnFeedEnabled != null) this.commentOnFeedEnabled = commentOnFeedEnabled;
        if (replyOnCommentEnabled != null) this.replyOnCommentEnabled = replyOnCommentEnabled;
        if (todayFeedEnabled != null) this.todayFeedEnabled = todayFeedEnabled;
        if (hotTopicRoomEnabled != null) this.hotTopicRoomEnabled = hotTopicRoomEnabled;
        if (marketingEnabled != null) this.marketingEnabled = marketingEnabled;
    }

    // 알림 타입별 수신 여부
    public boolean acceptsType(NotificationType type) {
        return switch (type) {
            // 서비스 알림 — 타입별 토글
            case LIKE_FEED          -> likeFeedEnabled;
            case LIKE_REVIEW        -> likeReviewEnabled;
            case LIKE_COMMENT       -> likeCommentEnabled;
            case COMMENT_ON_FEED    -> commentOnFeedEnabled;
            case REPLY_ON_COMMENT   -> replyOnCommentEnabled;
            case TODAY_FEED         -> todayFeedEnabled;
            case HOT_TOPIC_ROOM     -> hotTopicRoomEnabled;
            // 마케팅/광고
            case MARKETING          -> marketingEnabled;
            // 운영 정책상 알림 — 수신 동의 무관, 항상 발송
            case REPORT_RECEIVED, REPORT_PROCESSED,
                 RESTRICTION_7D, RESTRICTION_30D,
                 TOS_UPDATE, PRIVACY_UPDATE, FEATURE_UPDATE -> true;
        };
    }
}
