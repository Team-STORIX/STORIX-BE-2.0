package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownNotificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownNotificationException();

    private UnknownNotificationException() { super(ErrorCode.NOTIFICATION_NOT_FOUND); }
}
