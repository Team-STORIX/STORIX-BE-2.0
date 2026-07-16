package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PopupOutOfEventPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PopupOutOfEventPeriodException();

    private PopupOutOfEventPeriodException() { super(ErrorCode.EVENT_POPUP_OUT_OF_EVENT_PERIOD); }
}
