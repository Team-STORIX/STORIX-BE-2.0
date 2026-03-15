package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownUserException();

    private UnknownUserException() { super(ErrorCode.NOT_FOUND); }
}