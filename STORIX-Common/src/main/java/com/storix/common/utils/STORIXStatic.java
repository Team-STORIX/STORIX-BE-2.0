package com.storix.common.utils;

import java.util.List;

public class STORIXStatic {
    public static final String BEARER = "Bearer ";
    public static final String TOKEN_TYPE = "type";
    public static final String TOKEN_ROLE = "role";
    public static final String TOKEN_ISSUR = "STORIX";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ONBOARDING_TOKEN = "onboarding_token";
    public static final String KID = "kid";
    public static final String WITHDRAW_PREFIX = "DELETED:";

    public static final int MILLI_TO_SECOND = 1000;

    // 인기 검색어 Redis 합산 키
    public static final String TRENDING_AGGREGATED_KEY = "search:trending:aggregated";
    public static final String TRENDING_PREV_AGGREGATED_KEY = "search:trending:aggregated:prev";

    public static final List<String> SWAGGER_URI= List.of(
            new String[]{"/swagger-resources/", "/swagger-ui/", "/v3/api-docs"}
    );

    // 알림 메시지 타이틀/본문 템플릿
    public static class Notification {

        // 타이틀 — 서비스
        public static final String TITLE_FEED       = "피드";
        public static final String TITLE_REVIEW     = "리뷰";
        public static final String TITLE_TOPIC_ROOM = "토픽룸";

        // 타이틀 — 운영 정책
        public static final String TITLE_REPORT_RECEIVED   = "신고 접수 안내";
        public static final String TITLE_REPORT_PROCESSED  = "신고 처리 완료";
        public static final String TITLE_RESTRICTION       = "서비스 이용 제한 안내";
        public static final String TITLE_TOS_UPDATE        = "약관 변경 안내";
        public static final String TITLE_PRIVACY_UPDATE    = "개인정보 처리방침 변경 안내";
        public static final String TITLE_FEATURE_UPDATE    = "신기능 업데이트";

        // 타이틀 — 마케팅/광고 (고정 prefix '(광고) ' + 운영자 입력)
        public static final String TITLE_MARKETING = "(광고) %s";

        // 본문 템플릿 — 유저 인터랙션
        public static final String TPL_LIKE_FEED        = "%s님이 회원님의 피드에 하트를 보냈어요.";
        public static final String TPL_LIKE_REVIEW      = "%s님이 회원님의 리뷰에 하트를 보냈어요.";
        public static final String TPL_LIKE_COMMENT     = "%s님이 회원님의 댓글에 하트를 보냈어요.";
        public static final String TPL_COMMENT_ON_FEED  = "%s님이 댓글을 남겼어요 : %s";
        public static final String TPL_REPLY_ON_COMMENT = "%s님이 대댓글을 남겼어요 : %s";

        public static final String TPL_TODAY_FEED      = "회원님의 피드가 오늘의 피드로 선정되었어요!";
        public static final String TPL_HOT_TOPIC_ROOM  = "회원님이 참여한 '%s' 토픽룸이 HOT으로 선정되었어요!";

        // 본문 템플릿 — 운영 정책
        public static final String TPL_REPORT_RECEIVED  = "회원님이 접수한 신고가 정상적으로 접수되었습니다.";
        public static final String TPL_REPORT_PROCESSED = "접수해주신 신고에 대한 검토 및 처리가 완료되었습니다.";
        public static final String TPL_RESTRICTION_7D   = "운영 정책 위반으로 인해 7일간 서비스 이용이 제한되었습니다.";
        public static final String TPL_RESTRICTION_30D  = "운영 정책 위반으로 인해 30일간 서비스 이용이 제한되었습니다.";
        public static final String TPL_TOS_UPDATE       = "서비스 이용약관이 일부 개정되어 안내드립니다.";
        public static final String TPL_PRIVACY_UPDATE   = "개인정보 처리방침이 변경되어 안내드립니다.";
        public static final String TPL_FEATURE_UPDATE   = "새로운 기능이 추가되어 안내드립니다.";

        // 본문 템플릿 — 마케팅/광고 (운영자 입력 + 고정 suffix '(수신거부 : 설정)')
        public static final String TPL_MARKETING = "%s (수신거부 : 설정)";

        // 댓글 본문 미리보기 — 10자 노출 후 ...
        public static final int CONTENT_PREVIEW_MAX = 10;
        public static final String CONTENT_PREVIEW_SUFFIX = "...";
    }
}
