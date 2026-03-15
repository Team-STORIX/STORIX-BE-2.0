package com.storix.storix_api.global.apiPayload.code;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

    // Common Error
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_001", "잘못된 요청입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_ERROR_002", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_ERROR_003", "접근이 금지되었습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_ERROR_004", "요청한 자원을 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_ERROR_005", "서버 내부 오류가 발생했습니다"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_006", "요청값이 올바르지 않습니다."),
    DATA_INTEGRITY_VIOLATION_REQUEST(HttpStatus.CONFLICT, "COMMON_ERROR_007", "DB 데이터 무결성 조건 위반입니다. 백엔드에게 연락주세요."),
    INVALID_JSON_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_ERROR_008", "요청 JSON 형식이 잘못되었습니다. 백엔드에게 문의주세요."),
    UNHANDLED_ERROR(HttpStatus.BAD_REQUEST, "COMMON_ERROR_009", "핸들링하지 않은 에러입니다. 백엔드에게 연락주세요."),

    // Token error
    TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "TOKEN_ERROR_001", "인가가 필요한 경로로 토큰이 전달되지 않았습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_ERROR_002", "잘못된 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_ERROR_003", "토큰이 만료되었습니다. 토큰을 재 발급 해주세요"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "TOKEN_ERROR_004", "토큰이 만료되었습니다. 재로그인 해주세요"),
    ONBOARDING_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_ERROR_005", "온보딩 토큰이 만료되었습니다. 소셜 로그인 재시도 해주세요"),
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "COOKIE_ERROR_001", "쿠키가 만료되었거나 저장되지 않았습니다. 로그인 해주세요."),
    REFRESH_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "COOKIE_ERROR_002", "쿠키에 저장된 리프레쉬 토큰이 만료되었거나 쿠키가 비어있습니다. 재로그인 해주세요."),

    // Auth error
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "LOGIN_ERROR_001", "아이디 또는 비밀번호가 일치하지 않습니다"),
    DUPLICATE_USER_SIGN(HttpStatus.BAD_REQUEST, "SIGNUP_ERROR_001", "중복 가입 요청입니다."),
    ONBOARDING_INVALID_WORKS(HttpStatus.BAD_REQUEST, "SIGNUP_ERROR_002", "서버 DB에 적재된 온보딩 작품 리스트와 관심 작품 리스트 정보가 다릅니다."),
    INVALID_USER_LOGOUT(HttpStatus.BAD_REQUEST, "LOGOUT_ERROR_001", "이미 로그아웃 처리가 되었거나, 다른 소셜 계정에 대한 로그아웃 요청입니다."),
    INVALID_USER_WITHDRAW(HttpStatus.BAD_REQUEST, "WITHDRAW_ERROR_001", "이미 탈퇴 처리된 사용자입니다."),
    ONBOARDING_DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "NICKNAME_ERROR_001", "이미 사용 중인 닉네임입니다."),
    PROFILE_DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "NICKNAME_ERROR_002", "이미 사용 중인 닉네임입니다."),
    PROFILE_FORBIDDEN_NICKNAME(HttpStatus.BAD_REQUEST, "NICKNAME_ERROR_003", "사용할 수 없는 닉네임입니다."),
    PROFILE_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "NICKNAME_ERROR_004", "금칙어가 포함된 닉네임입니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "USER_ERROR_001", "로그인이 필요합니다."),
    FORBIDDEN_APPROACH(HttpStatus.FORBIDDEN, "USER_ERROR_002", "해당 요청을 수행할 권한이 없습니다."),

    INVALID_ROLE_ERROR(HttpStatus.UNAUTHORIZED, "ROLE_ERROR_001", "잘못된 role값 입니다."),

    // Profile error
    PROFILE_IMAGE_NOT_EXIST(HttpStatus.BAD_REQUEST, "PROFILE_ERROR_001", "업로드한 프로필 사진의 objectKey값을 보내주세요."),

    // Image error
    IMAGE_INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_ERROR_001", "지원하지 않는 Content Type입니다."),

    // Other Server error
    OTHER_SERVER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_1", "Other server bad request"),
    OTHER_SERVER_UNAUTHORIZED(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_2", "Other server unauthorized"),
    OTHER_SERVER_FORBIDDEN(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_3", "Other server forbidden"),
    OTHER_SERVER_EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_4", "Other server expired token"),
    OTHER_SERVER_NOT_FOUND(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_5", "Other server not found error"),
    OTHER_SERVER_INTERNAL_SERVER_ERROR(HttpStatus.BAD_REQUEST, "FEIGN_ERROR_6", "Other server internal server error"),

    // Kakao OAuth error
    KOE009(HttpStatus.BAD_REQUEST, "KAKAO_KOE009", "등록되지 않은 플랫폼에서 액세스 토큰을 요청 하는 경우"),
    KOE010(HttpStatus.BAD_REQUEST, "KAKAO_KOE101", "클라이언트 시크릿(Client secret) 기능을 사용하는 앱에서 토큰 요청 시 client_secret 값을 전달하지 않거나 정확하지 않은 값을 전달하는 경우"),
    KOE303(HttpStatus.BAD_REQUEST, "KAKAO_KOE303", "인가 코드 요청 시 사용한 redirect_uri와 액세스 토큰 요청 시 사용한 redirect_uri가 다릅니다"),
    KOE320(HttpStatus.BAD_REQUEST, "KAKAO_KOE320", "동일한 인가 코드를 두 번 이상 사용하거나, 이미 만료된 인가 코드를 사용한 경우, 혹은 인가 코드를 찾을 수 없는 경우입니다."),
    KOE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "KAKAO_KOE_INVALID_REQUEST","잘못된 요청인 경우"),

    // Naver OAuth error
    NOE024(HttpStatus.UNAUTHORIZED, "NAVER_NOE024", "OAuth 인증에 실패했습니다."),
    NOE028(HttpStatus.UNAUTHORIZED, "NAVER_NOE028", "OAuth 인증 헤더가 없습니다."),
    NOE403(HttpStatus.FORBIDDEN, "NAVER_NOE403", "OAuth 호출 권한이 없습니다."),
    NOE404(HttpStatus.NOT_FOUND, "NAVER_NOE404", "OAuth 검색 결과가 없습니다."),
    NOE500(HttpStatus.BAD_REQUEST, "NAVER_NOE500", "네이버 데이터 베이스 오류입니다."),
    NOE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "NAVER_NOE_INVALID_REQUEST", "파라미터가 잘못되었거나 요청문이 잘못되었습니다."),
    NOE_UNAUTHORIZED_CLIENT(HttpStatus.BAD_REQUEST, "NAVER_NOE_UNAUTHORIZED_CIENT", "인증받지 않은 인증 코드로 요청했습니다."),
    NOE_UNSUPPORTED_RESPONSE_TYPE(HttpStatus.BAD_REQUEST, "NAVER_NOE_UNSUPPORTED_RESPONSE_TYPE", "정의되지 않은 반환 형식으로 요청했습니다."),
    NOE_SERVER_ERROR(HttpStatus.BAD_REQUEST, "NAVER_SERVER_ERROR", "네이버 인증 서버의 오류로 요청을 처리하지 못했습니다."),

    // OIDC error
    OIDC_OLD_PUBLIC_KEY_ERROR(HttpStatus.BAD_REQUEST, "OIDC_ERORR_1", "OIDC 공개키 갱신이 필요합니다."),

    // Notification error
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_ERROR_001", "알림을 찾을 수 없습니다"),
    NOTIFICATION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "NOTIFICATION_ERROR_002", "인가되지 않은 접근입니다."),

    // Topic Room error
    ADULT_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "TOPIC_ROOM_ERROR_001", "성인인증이 되지 않은 사용자입니다."),
    TOPIC_ROOM_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "TOPIC_ROOM_ERROR_002", "토픽룸 최대 개수는 9개입니다."),
    TOPIC_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC_ROOM_ERROR_003", "해당 토픽룸을 찾을 수 없습니다."),
    INVALID_TOPIC_ROOM_TITLE(HttpStatus.BAD_REQUEST, "TOPIC_ROOM_ERROR_004", "토픽룸에 금칙어가 포함되어 있습니다."),
    ALREADY_JOINED_ROOM(HttpStatus.CONFLICT, "TOPIC_ROOM_ERROR_005", "이미 참여 중인 토픽룸입니다."),
    SELF_REPORT_ERROR(HttpStatus.BAD_REQUEST, "TOPIC_ROOM_ERROR_006", "자기 자신은 신고할 수 없습니다."),
    TOPIC_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "TOPIC_ROOM_ERROR_007", "이미 해당 작품에 대한 토픽룸이 존재합니다."),
    TOPIC_ROOM_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "TOPIC_ROOM_ERROR_008", "해당 토픽룸에 참여하지 않은 유저입니다."),

    // Chat error
    CHAT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_ERROR_001", "채팅 메시지 발행 중 서버 관리자에게 문의 바랍니다."),
    CHAT_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_ERROR_002", "Redis 연결 실패로 메시지 전송이 불가합니다. 서버 관리자에게 문의 바랍니다."),


    // Works error
    WORKS_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKS_ERROR_001", "해당 작품을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKS_ERROR_002", "해당 리뷰 정보를 찾을 수 없습니다."),
    REVIEW_UPDATE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "WORKS_ERROR_003", "리뷰 수정 요청 처리 중 문제가 발생하였습니다."),
    REVIEW_DELETE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "WORKS_ERROR_004", "리뷰 삭제 요청 처리 중 문제가 발생하였습니다."),
    DUPLICATE_REVIEW_USER_REPORT(HttpStatus.BAD_REQUEST, "WORKS_ERROR_005", "이미 신고가 완료된 리뷰입니다."),
    INVALID_REVIEW_USER_REPORT(HttpStatus.BAD_REQUEST, "WORKS_ERROR_006", "신고 정보가 올바르지 않습니다."),

    // Favorite error
    FAVORITE_WORKS_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "FAVORITE_ERROR_001", "이미 관심 작품 해제가 되었거나, 관심 작품으로 등록한 적 없는 작품입니다."),
    FAVORITE_ARTIST_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "FAVORITE_ERROR_002", "이미 관심 작가 해제가 되었거나, 관심 작가로 등록한 적 없는 작가입니다."),
    FAVORITE_WORKS_DUPLICATE_REQUEST(HttpStatus.BAD_REQUEST, "FAVORITE_ERROR_003", "이미 관심 작품 등록이 된 작품입니다."),
    FAVORITE_ARTIST_DUPLICATE_REQUEST(HttpStatus.BAD_REQUEST, "FAVORITE_ERROR_004", "이미 관심 작가 등록이 된 작가입니다."),
    FAVORITE_ARTIST_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE_ERROR_005", "작가 계정이 아닙니다."),
  
    // Plus error
    PLUS_INVALID_RATING(HttpStatus.BAD_REQUEST, "PLUS_ERROR_001", "Enum 필드와 매핑할 수 없는 잘못된 평점값입니다."),
    PLUS_INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "PLUS_ERROR_002", "지원하지 않는 Content Type입니다."),
    PLUS_DUPLICATE_BOARD_UPLOAD(HttpStatus.BAD_REQUEST, "PLUS_ERROR_003", "중복 게시글 업로드 요청입니다."),
    PLUS_DUPLICATE_REVIEW_UPLOAD(HttpStatus.BAD_REQUEST, "PLUS_ERROR_004", "해당 작품에 대한 리뷰가 이미 존재합니다."),
    PLUS_WORKS_NOT_EXIST(HttpStatus.BAD_REQUEST, "PLUS_ERROR_005", "존재하지 않는 작품입니다."),
    PLUS_WORKS_ID_NOT_EXIST(HttpStatus.BAD_REQUEST, "PLUS_ERROR_006", "작품 id값을 보내주세요."),
    PLUS_IMAGE_NOT_EXIST(HttpStatus.BAD_REQUEST, "PLUS_ERROR_007", "게시물 이미지 objectKey 값이 잘못되었습니다."),

    // Feed error
    BOARD_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "BOARD_ERROR_001", "이미 삭제된 게시물이거나 정보를 찾을 수 없습니다."),
    REVIEW_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "REVIEW_ERROR_001", "이미 삭제된 리뷰이거나 정보를 찾을 수 없습니다."),
    DUPLICATE_FEED_USER_REPORT(HttpStatus.BAD_REQUEST, "FEED_ERROR_001", "이미 신고가 완료된 게시물입니다."),
    DUPLICATE_FEED_REPLY_USER_REPORT(HttpStatus.BAD_REQUEST, "FEED_ERROR_002", "이미 신고가 완료된 댓글입니다."),
    BOARD_REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "REPLY_ERROR_001", "해당 게시글에 대한 댓글 정보를 찾을 수 없습니다."),

    // Preference error
    PREFERENCE_ALREADY_DONE_TODAY(HttpStatus.BAD_REQUEST, "PREFERENCE_ERROR_001", "취향 탐색 기능은 하루에 한 번만 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
