package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ExpiredRefreshTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredRefreshTokenException();

    private ExpiredRefreshTokenException() { super(ErrorCode.REFRESH_TOKEN_EXPIRED); }
}