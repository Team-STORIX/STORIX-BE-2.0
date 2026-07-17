package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventInvalidPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventInvalidPeriodException();

    private AppEventInvalidPeriodException() { super(ErrorCode.ADMIN_APP_EVENT_INVALID_PERIOD); }
}
