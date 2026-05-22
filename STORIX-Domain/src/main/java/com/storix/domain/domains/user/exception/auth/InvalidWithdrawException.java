package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidWithdrawException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidWithdrawException();

    private InvalidWithdrawException() { super(ErrorCode.INVALID_USER_WITHDRAW); }
}
