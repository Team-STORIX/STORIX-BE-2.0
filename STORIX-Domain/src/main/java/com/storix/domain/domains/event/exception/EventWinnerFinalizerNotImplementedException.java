package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class EventWinnerFinalizerNotImplementedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new EventWinnerFinalizerNotImplementedException();

    private EventWinnerFinalizerNotImplementedException() { super(ErrorCode.APP_EVENT_WINNER_FINALIZER_NOT_IMPLEMENTED); }
}
