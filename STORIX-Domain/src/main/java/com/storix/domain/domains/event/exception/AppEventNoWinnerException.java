package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventNoWinnerException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventNoWinnerException();

    private AppEventNoWinnerException() { super(ErrorCode.APP_EVENT_NO_WINNER); }
}
