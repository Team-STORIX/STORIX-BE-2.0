package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownNotificationSettingException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownNotificationSettingException();

    private UnknownNotificationSettingException() { super(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND); }
}
