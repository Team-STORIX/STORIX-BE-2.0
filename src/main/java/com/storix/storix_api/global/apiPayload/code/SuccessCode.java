package com.storix.storix_api.global.apiPayload.code;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum SuccessCode {

    SUCCESS(HttpStatus.OK, "COMMON_SUCCESS_001", "정상적인 요청입니다."),
    CREATED(HttpStatus.CREATED, "COMMON_SUCCESS_002", "정상적으로 생성되었습니다."),

    // Notification success
    NOTIFICATION_LOAD_SUCCESS(HttpStatus.OK, "NOTI2001", "알림 목록을 성공적으로 조회했습니다."),
    NOTIFICATION_COUNT_SUCCESS(HttpStatus.OK, "NOTI2002", "안 읽은 알림 개수를 성공적으로 조회했습니다."),
    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "NOTI2003", "알림을 읽음 처리했습니다."),
    NOTIFICATION_READ_ALL_SUCCESS(HttpStatus.OK, "NOTI2004", "모든 알림을 읽음 처리했습니다."),

    // Auth success
    OAUTH_LOGIN_SUCCESS(HttpStatus.OK, "OAUTH_SUCCESS_001", "소셜 로그인에 성공했습니다."),
    OAUTH_PRE_LOGIN_SUCCESS(HttpStatus.OK, "OAUTH_SUCCESS_002", "소셜 로그인에 성공했습니다. 회원가입이 필요합니다."),
    AUTH_NICKNAME_SUCCESS(HttpStatus.OK, "NICKNAME_SUCCESS_001", "사용 가능한 닉네임입니다."),
    AUTH_SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_SUCCESS_001", "유저 정보 등록이 완료되었습니다."),
    AUTH_LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_SUCCESS_002", "로그아웃에 성공했습니다."),
    AUTH_REISSUE_ACCESSTOKEN_SUCCESS(HttpStatus.CREATED, "AUTH_SUCCESS_003", "엑세스 토큰 재발급에 성공했습니다."),
    AUTH_ARTIST_SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH_SUCCESS_004", "작가 계정 생성이 완료되었습니다."),
    AUTH_ARTIST_LOGIN_SUCCESS(HttpStatus.OK, "AUTH_SUCCESS_005", "작가 계정 로그인에 성공했습니다."),
    AUTH_WITHDRAW_SUCCESS(HttpStatus.OK, "AUTH_SUCCESS_006", "회원 탈퇴에 성공했습니다."),
    ONBOARDING_WORKS_LIST_LOAD_SUCCESS(HttpStatus.OK, "ONBOARDING_SUCCESS_001", "온보딩 작품 리스트 조회에 성공했습니다."),

    // Home success
    HOME_TODAY_FEED_LOAD_SUCCESS(HttpStatus.OK, "HOME_SUCCESS_001", "오늘의 피드 조회에 성공했습니다."),

    // Profile success
    PROFILE_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_001", "유저 정보 조회에 성공했습니다."),
    PROFILE_NICKNAME_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_002", "변경 가능한 닉네임입니다."),
    PROFILE_UPDATE_NICKNAME_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_003", "닉네임 변경에 성공했습니다."),
    PROFILE_UPDATE_DESCRIPTION_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_004", "한 줄 소개 변경에 성공했습니다."),
    PROFILE_UPDATE_IMAGE_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_005", "프로필 이미지 변경에 성공했습니다."),
    PROFILE_FAVORITE_ARTIST_LIST_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_006", "프로필 관심 작가 리스트 조회에 성공했습니다."),
    PROFILE_FAVORITE_WORKS_LIST_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_007", "프로필 관심 작품 리스트 조회에 성공했습니다."),
    PROFILE_MY_BOARDS_LIST_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_008", "프로필 내 활동 게시글 리스트 조회에 성공했습니다."),
    PROFILE_MY_BOARDS_REPLY_LIST_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_009", "프로필 내 활동 댓글 리스트 조회에 성공했습니다."),
    PROFILE_MY_BOARDS_LIKE_LIST_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_010", "프로필 내 활동 좋아요 리스트 조회에 성공했습니다."),
    PROFILE_RATING_DISTRIBUTION_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_011", "프로필 리뷰 별점 분포 조회에 성공했습니다."),
    PROFILE_FAVORITE_HASHTAGS_LOAD_SUCCESS(HttpStatus.OK, "PROFILE_SUCCESS_012", "프로필 선호 해시태그 조회에 성공했습니다."),

    // Image success
    IMAGE_ISSUE_PRESIGNED_URL_SUCCESS(HttpStatus.OK, "IMAGE_SUCCESS_001", "이미지를 업로드할 Presigned Url 발급에 성공했습니다."),

    // Plus success
    PLUS_BOARD_UPLOAD_SUCCESS(HttpStatus.CREATED, "PLUS_SUCCESS_001", "게시물 등록에 성공했습니다."),
    PLUS_REVIEW_UPLOAD_SUCCESS(HttpStatus.CREATED,"PLUS_SUCCESS_002", "리뷰 등록에 성공했습니다."),
    PLUS_WORKS_LOAD_SUCCESS(HttpStatus.OK, "PLUS_SUCCESS_003", "작품 정보 조회에 성공했습니다."),
    PLUS_REVIEW_CHECK_SUCCESS(HttpStatus.OK, "PLUS_SUCCESS_004", "리뷰 작성이 가능합니다."),

    // Works success
    WORKS_DETAIL_MY_REVIEW_LOAD_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_001", "내 리뷰 조회에 성공했습니다."),
    WORKS_DETAIL_OTHER_REVIEW_LOAD_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_002", "다른 유저 리뷰 조회에 성공했습니다."),
    WORKS_DETAIL_REVIEW_DETAIL_LOAD_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_003", "리뷰 단건 조회에 성공했습니다."),
    WORKS_DETAIL_REVIEW_LIKE_SUCCESS(HttpStatus.CREATED, "WORKS_DETAIL_SUCCESS_004", "리뷰 좋아요 토글링에 성공했습니다."),
    WORKS_DETAIL_REVIEW_UPDATE_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_005", "내 리뷰 수정에 성공했습니다."),
    WORKS_DETAIL_REVIEW_DELETE_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_006", "내 리뷰 삭제에 성공했습니다."),
    WORKS_DETAIL_REVIEW_REPORT_SUCCESS(HttpStatus.OK, "WORKS_DETAIL_SUCCESS_007", "리뷰 신고에 성공했습니다."),

    // Feed success
    FEED_ALL_READER_BOARD_LOAD_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_001", "관심 작품 피드 전체 게시글 조회에 성공했습니다."),
    FEED_WORKS_READER_BOARD_LOAD_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_002", "관심 작품 피드 게시글 리스트 조회에 성공했습니다."),
    FEED_READER_BOARD_LOAD_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_003", "관심 작품 피드 게시글 단건 조회에 성공했습니다."),
    FEED_READER_BOARD_LIKE_SUCCESS(HttpStatus.CREATED, "FEED_SUCCESS_004", "관심 작품 피드 게시글 좋아요 토글링에 성공했습니다."),
    FEED_READER_BOARD_REPLY_UPLOAD_SUCCESS(HttpStatus.CREATED, "FEED_SUCCESS_005", "관심 작품 피드 게시글 댓글 등록에 성공했습니다."),
    FEED_READER_BOARD_REPLY_LIKE_SUCCESS(HttpStatus.CREATED, "FEED_SUCCESS_006", "관심 작품 피드 게시글 댓글 좋아요 토글링에 성공했습니다."),
    FEED_READER_BOARD_DELETE_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_007", "관심 작품 피드 내 게시물 삭제에 성공했습니다."),
    FEED_READER_BOARD_REPORT_SUCCESS(HttpStatus.CREATED, "FEED_SUCCESS_008", "관심 작품 피드 게시글 신고에 성공했습니다."),
    FEED_READER_BOARD_REPLY_DELETE_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_009", "관심 작품 피드 내 댓글 삭제에 성공했습니다."),
    FEED_READER_BOARD_REPLY_REPORT_SUCCESS(HttpStatus.CREATED, "FEED_SUCCESS_010", "관심 작품 피드 댓글 신고에 성공했습니다."),
    FEED_FAVORITE_WORKS_INFO_LOAD_SUCCESS(HttpStatus.OK, "FEED_SUCCESS_011", "관심 작품 피드 리스트 조회에 성공했습니다."),

    // Favorite success
    FAVORITE_WORKS_LOAD_SUCCESS(HttpStatus.OK, "FAVORITE_SUCCESS_001", "관심 작품 등록 여부 조회에 성공했습니다."),
    FAVORITE_WORKS_ADD_SUCCESS(HttpStatus.CREATED, "FAVORITE_SUCCESS_002", "관심 작품 등록에 성공했습니다."),
    FAVORITE_WORKS_DELETE_SUCCESS(HttpStatus.OK, "FAVORITE_SUCCESS_003", "관심 작품 등록 해제에 성공했습니다."),
    FAVORITE_ARTIST_LOAD_SUCCESS(HttpStatus.OK, "FAVORITE_SUCCESS_004", "관심 작가 등록 여부 조회에 성공했습니다."),
    FAVORITE_ARTIST_ADD_SUCCESS(HttpStatus.CREATED, "FAVORITE_SUCCESS_005", "관심 작가 등록에 성공했습니다."),
    FAVORITE_ARTIST_DELETE_SUCCESS(HttpStatus.OK, "FAVORITE_SUCCESS_006", "관심 작가 등록 해제에 성공했습니다."),

    // Library success
    LIBRARY_WORKS_LOAD_SUCCESS(HttpStatus.OK, "LIBRARY_SUCCESS_001", "서재 정보 조회에 성공했습니다."),
    LIBRARY_SEARCH_SUCCESS(HttpStatus.OK, "LIBRARY_SUCCESS_002", "서재 내 검색에 성공했습니다."),
    LIBRARY_RECENT_LOAD_SUCCESS(HttpStatus.OK, "LIBRARY_SUCCESS_003", "서재 최근 검색어 조회에 성공했습니다."),
    LIBRARY_RECENT_REMOVE_SUCCESS(HttpStatus.OK, "LIBRARY_SUCCESS_004", "서재 최근 검색어 삭제에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
