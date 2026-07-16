package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UserAppEventNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UserAppEventNotFoundException();

    private UserAppEventNotFoundException() { super(ErrorCode.APP_EVENT_NOT_FOUND); }
}
