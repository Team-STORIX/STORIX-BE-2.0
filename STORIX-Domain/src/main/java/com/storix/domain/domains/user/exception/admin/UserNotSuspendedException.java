package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UserNotSuspendedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UserNotSuspendedException();

    private UserNotSuspendedException() {
        super(ErrorCode.USER_NOT_SUSPENDED);
    }
}
