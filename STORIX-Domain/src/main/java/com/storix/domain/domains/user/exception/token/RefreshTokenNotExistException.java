package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCookieException;

public class RefreshTokenNotExistException extends STORIXCookieException {

    public static final STORIXCookieException EXCEPTION = new RefreshTokenNotExistException();

    private RefreshTokenNotExistException() { super(ErrorCode.REFRESH_TOKEN_NOT_EXIST); }
}
