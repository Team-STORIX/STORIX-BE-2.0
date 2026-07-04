package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminNotificationNotUpdatableException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminNotificationNotUpdatableException();

    private AdminNotificationNotUpdatableException() { super(ErrorCode.ADMIN_NOTIFICATION_NOT_UPDATABLE); }
}
