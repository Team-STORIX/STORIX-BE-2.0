package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AlreadyWithDrawUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyWithDrawUserException();

    private AlreadyWithDrawUserException() { super(ErrorCode.INVALID_USER_WITHDRAW); }
}
