package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventPeriodRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventPeriodRequiredException();

    private AppEventPeriodRequiredException() { super(ErrorCode.ADMIN_APP_EVENT_PERIOD_REQUIRED); }
}
