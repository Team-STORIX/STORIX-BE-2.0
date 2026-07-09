package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UserAppEventForbiddenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UserAppEventForbiddenException();

    private UserAppEventForbiddenException() { super(ErrorCode.APP_EVENT_FORBIDDEN); }
}
