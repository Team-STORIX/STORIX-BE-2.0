package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidLogoutException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidLogoutException();

    private InvalidLogoutException() { super(ErrorCode.INVALID_USER_LOGOUT); }
}
