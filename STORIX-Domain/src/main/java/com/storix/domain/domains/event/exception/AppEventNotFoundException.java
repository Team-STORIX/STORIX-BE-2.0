package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventNotFoundException();

    private AppEventNotFoundException() { super(ErrorCode.ADMIN_APP_EVENT_NOT_FOUND); }
}
