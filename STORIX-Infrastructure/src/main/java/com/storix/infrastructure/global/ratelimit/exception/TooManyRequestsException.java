package com.storix.infrastructure.global.ratelimit.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TooManyRequestsException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TooManyRequestsException();

    private TooManyRequestsException() {
        super(ErrorCode.TOO_MANY_REQUESTS);
    }
}
