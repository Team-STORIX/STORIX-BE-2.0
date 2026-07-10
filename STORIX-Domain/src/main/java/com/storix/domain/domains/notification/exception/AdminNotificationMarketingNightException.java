package com.storix.domain.domains.notification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminNotificationMarketingNightException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminNotificationMarketingNightException();

    private AdminNotificationMarketingNightException() { super(ErrorCode.ADMIN_NOTIFICATION_MARKETING_NIGHT_BLOCKED); }
}
