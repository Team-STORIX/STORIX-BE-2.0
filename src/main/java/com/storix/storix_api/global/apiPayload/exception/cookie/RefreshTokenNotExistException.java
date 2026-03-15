package com.storix.storix_api.global.apiPayload.exception.cookie;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCookieException;

public class RefreshTokenNotExistException extends STORIXCookieException {

    public static final STORIXCookieException EXCEPTION = new RefreshTokenNotExistException();

    private RefreshTokenNotExistException() { super(ErrorCode.REFRESH_TOKEN_NOT_EXIST); }
}
