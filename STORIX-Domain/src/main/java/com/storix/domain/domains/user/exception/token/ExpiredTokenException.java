package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ExpiredTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredTokenException();

    private ExpiredTokenException() { super(ErrorCode.TOKEN_EXPIRED); }
}