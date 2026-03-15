package com.storix.storix_api.global.apiPayload.exception.cookie;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCookieException;

public class InvalidRefreshTokenException extends STORIXCookieException {

    public static final STORIXCookieException EXCEPTION = new InvalidRefreshTokenException();

    private InvalidRefreshTokenException() { super(ErrorCode.INVALID_TOKEN); }
}
