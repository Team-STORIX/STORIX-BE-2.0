package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() { super(ErrorCode.INVALID_TOKEN); }
}
