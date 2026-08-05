package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminNotificationEventNoWinnerException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminNotificationEventNoWinnerException();

    private AdminNotificationEventNoWinnerException() { super(ErrorCode.ADMIN_NOTIFICATION_EVENT_NO_WINNER); }
}
