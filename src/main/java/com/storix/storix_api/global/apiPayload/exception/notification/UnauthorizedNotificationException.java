package com.storix.storix_api.global.apiPayload.exception.notification;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnauthorizedNotificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnauthorizedNotificationException();

    private UnauthorizedNotificationException() { super(ErrorCode.NOTIFICATION_UNAUTHORIZED); }

}
