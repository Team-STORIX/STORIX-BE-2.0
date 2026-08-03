package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventInvalidWinnerCountException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventInvalidWinnerCountException();

    private AppEventInvalidWinnerCountException() { super(ErrorCode.APP_EVENT_INVALID_WINNER_COUNT); }
}
