package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UserAlreadySuspendedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UserAlreadySuspendedException();

    private UserAlreadySuspendedException() {
        super(ErrorCode.USER_ALREADY_SUSPENDED);
    }
}
