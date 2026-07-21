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
    public static final String WITHDRAWN_NICK_NAME = "탈퇴한 유저";

    // 일반 유저 선택 불가 닉네임
    public static final List<String> RESERVED_NICK_NAMES = List.of(
            WITHDRAWN_NICK_NAME,
            "관리자",
            "운영자",
            "운영진",
            "개발자",
            "스토릭스",
            "STORIX"
    );

    // 관리자/개발자 닉네임 중복 우회용 suffix 구분자
    public static final String NICK_NAME_SUFFIX_DELIMITER = ":";

    // JPQL @Query에서 탈퇴 유저 닉네임 마스킹 + 관리자/개발자 suffix 제거용
    public static final String NICK_NAME_DISPLAY_CASE_WHEN =
            "CASE WHEN u.deletedAt IS NOT NULL THEN '" + WITHDRAWN_NICK_NAME + "' " +
            "WHEN LOCATE('" + NICK_NAME_SUFFIX_DELIMITER + "', u.nickName) > 0 " +
            "THEN SUBSTRING(u.nickName, 1, LOCATE('" + NICK_NAME_SUFFIX_DELIMITER + "', u.nickName) - 1) " +
            "ELSE u.nickName END";

    public static final int MILLI_TO_SECOND = 1000;

    public static final int HARD_DELETE_CHUNK_SIZE = 1000;

    // S3 DeleteObjects API 요청당 최대 키 수 (고정 한도가 1000)
    public static final int S3_MAX_KEYS_PER_DELETE_REQUEST = 1000;

    public static final List<String> SWAGGER_URI= List.of(
            new String[]{"/swagger-resources/", "/swagger-ui/", "/v3/api-docs"}
    );

    // JWT 인증 필터를 타지 않는 URI
    public static final List<String> PERMIT_ALL_URI = List.of(
            "/api/v1/app-version/",
            "/api/v1/onboarding/",
            "/api/v1/auth/oauth/",
            "/api/v1/auth/nickname/valid",
            "/api/v1/auth/users/reader/signup",
            "/api/v2/auth/users/reader/signup",
            "/api/v1/auth/tokens/refresh",

            "/api/v1/auth/tester/signup",
            "/api/v1/auth/tester/login",
            "/api/v1/auth/tester/slack/callback",

            "/api/v1/auth/admin/signup",
            "/api/v1/auth/admin/login",
            "/api/v1/auth/admin/slack/callback"
    );

    // MDC 로그 상관키
    public static class Mdc {
        public static final String TRACE_ID = "traceId";
        public static final String USER_ID = "userId";
        public static final String ENDPOINT = "endpoint";
        public static final String HTTP_METHOD = "httpMethod";
        public static final String ROLE = "role";
        public static final String RECIPIENT_USER_ID = "recipientUserId";
        public static final String ADMIN_NOTIFICATION_ID = "adminNotificationId";
    }

    // S3 업로드 객체 키 prefix
    public static class S3Prefix {
        public static final String BOARD   = "public/board/reader";
        public static final String PROFILE = "public/profile";
        // 앱 이벤트 이미지 base prefix. 실제 경로는 EVENT + "/{appEventId}/{surface}"
        public static final String EVENT   = "public/event";
    }

    // 알림 메시지 타이틀/본문 템플릿
    public static class Notification {

        // Android 헤드업 표시용 채널 id
        public static final String ANDROID_CHANNEL_ID = "storix_default_high";

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

    // 사용자 이력 — 마케팅 동의/거부 모달 표시 문구
    public static class UserHistory {

        // 처리자
        public static final String PROCESSOR_TEAM_STORIX = "팀 스토릭스";

        // 타이틀
        public static final String TITLE_MARKETING_AGREE  = "이벤트/혜택 알림 동의 안내";
        public static final String TITLE_MARKETING_REJECT = "이벤트/혜택 알림 거부 안내";

        // 처리 내용
        public static final String DESC_MARKETING_AGREE   = "알림 동의 처리 완료";
        public static final String DESC_MARKETING_REJECT  = "알림 거부 처리 완료";
    }
}
