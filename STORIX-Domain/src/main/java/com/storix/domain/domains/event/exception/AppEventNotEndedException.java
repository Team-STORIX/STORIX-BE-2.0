package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventNotEndedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventNotEndedException();

    private AppEventNotEndedException() { super(ErrorCode.APP_EVENT_NOT_ENDED); }
}
