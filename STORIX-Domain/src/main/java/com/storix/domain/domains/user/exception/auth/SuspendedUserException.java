package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SuspendedUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SuspendedUserException();

    private SuspendedUserException() {
        super(ErrorCode.SUSPENDED_USER);
    }
}
