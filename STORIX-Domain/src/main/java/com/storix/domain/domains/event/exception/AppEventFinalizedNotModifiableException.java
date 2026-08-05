package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventFinalizedNotModifiableException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventFinalizedNotModifiableException();

    private AppEventFinalizedNotModifiableException() { super(ErrorCode.ADMIN_APP_EVENT_FINALIZED_NOT_MODIFIABLE); }
}
