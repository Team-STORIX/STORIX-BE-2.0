package com.storix.storix_api.global.apiPayload.exception.notification;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownNotificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownNotificationException();

    private UnknownNotificationException() { super(ErrorCode.NOTIFICATION_NOT_FOUND); }
}
