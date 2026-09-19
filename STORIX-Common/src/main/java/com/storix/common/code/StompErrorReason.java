package com.storix.common.code;

import org.springframework.http.HttpStatus;

public enum StompErrorReason {

    UNAUTHORIZED,

    FORBIDDEN,

    BAD_REQUEST,

    INTERNAL_ERROR;

    public static StompErrorReason from(ErrorCode errorCode) {
        return from(errorCode.getHttpStatus());
    }

    public static StompErrorReason from(int statusCode) {
        return from(HttpStatus.valueOf(statusCode));
    }

    private static StompErrorReason from(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return UNAUTHORIZED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return FORBIDDEN;
        }
        if (status.is5xxServerError()) {
            return INTERNAL_ERROR;
        }

        return BAD_REQUEST;
    }
}
