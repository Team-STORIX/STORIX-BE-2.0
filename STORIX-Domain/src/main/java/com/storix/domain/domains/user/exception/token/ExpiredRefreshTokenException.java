package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ExpiredRefreshTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredRefreshTokenException();

    private ExpiredRefreshTokenException() { super(ErrorCode.REFRESH_TOKEN_EXPIRED); }
}