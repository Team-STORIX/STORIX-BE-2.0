package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidRoleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidRoleException();

    private InvalidRoleException() { super(ErrorCode.INVALID_ROLE_ERROR); }
}
