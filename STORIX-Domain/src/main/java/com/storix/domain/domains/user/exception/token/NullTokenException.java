package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class NullTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new NullTokenException();

    private NullTokenException() { super(ErrorCode.TOKEN_NOT_EXIST); }
}
