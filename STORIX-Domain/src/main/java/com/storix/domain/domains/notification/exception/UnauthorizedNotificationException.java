package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnauthorizedNotificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnauthorizedNotificationException();

    private UnauthorizedNotificationException() { super(ErrorCode.NOTIFICATION_UNAUTHORIZED); }

}
