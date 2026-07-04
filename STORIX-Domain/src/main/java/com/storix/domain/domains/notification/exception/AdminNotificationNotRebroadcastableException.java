package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminNotificationNotRebroadcastableException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminNotificationNotRebroadcastableException();

    private AdminNotificationNotRebroadcastableException() { super(ErrorCode.ADMIN_NOTIFICATION_NOT_REBROADCASTABLE); }
}
