package com.storix.infrastructure.external.notification.fcm;

import com.google.firebase.messaging.MessagingErrorCode;
import com.storix.infrastructure.external.notification.fcm.helper.FcmErrorClassifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[FCM] 에러코드 분류 (Firebase 가이드 3분류)")
class FcmErrorClassifierTest {

    private final FcmErrorClassifier helper = new FcmErrorClassifier();

    @Test
    @DisplayName("삭제 대상(A): UNREGISTERED, INVALID_ARGUMENT 만 true")
    void deletableTokens() {
        assertThat(helper.isDeletableToken(MessagingErrorCode.UNREGISTERED)).isTrue();
        assertThat(helper.isDeletableToken(MessagingErrorCode.INVALID_ARGUMENT)).isTrue();

        // 설정/인증 문제(B)는 삭제 대상 아님
        assertThat(helper.isDeletableToken(MessagingErrorCode.SENDER_ID_MISMATCH)).isFalse();
        assertThat(helper.isDeletableToken(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)).isFalse();
        // 일시 오류(C)도 삭제 대상 아님
        assertThat(helper.isDeletableToken(MessagingErrorCode.UNAVAILABLE)).isFalse();
        assertThat(helper.isDeletableToken(null)).isFalse();
    }

    @Test
    @DisplayName("일시 오류(C): UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED 만 true (재시도 대상)")
    void transientErrors() {
        assertThat(helper.isRetryableToken(MessagingErrorCode.UNAVAILABLE)).isTrue();
        assertThat(helper.isRetryableToken(MessagingErrorCode.INTERNAL)).isTrue();
        assertThat(helper.isRetryableToken(MessagingErrorCode.QUOTA_EXCEEDED)).isTrue();

        // 영구(A/B)는 재시도 대상 아님
        assertThat(helper.isRetryableToken(MessagingErrorCode.UNREGISTERED)).isFalse();
        assertThat(helper.isRetryableToken(MessagingErrorCode.INVALID_ARGUMENT)).isFalse();
        assertThat(helper.isRetryableToken(MessagingErrorCode.SENDER_ID_MISMATCH)).isFalse();
        assertThat(helper.isRetryableToken(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)).isFalse();
        assertThat(helper.isRetryableToken(null)).isFalse();
    }

    @Test
    @DisplayName("삭제(A)와 일시(C)는 상호 배타적 - 한 코드가 양쪽에 속하지 않음")
    void deletableAndTransientAreDisjoint() {
        for (MessagingErrorCode code : MessagingErrorCode.values()) {
            assertThat(helper.isDeletableToken(code) && helper.isRetryableToken(code))
                    .as("code=%s 가 삭제·일시 양쪽에 속함", code)
                    .isFalse();
        }
    }
}
