package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PopupInvalidDisplayPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PopupInvalidDisplayPeriodException();

    private PopupInvalidDisplayPeriodException() { super(ErrorCode.EVENT_POPUP_INVALID_DISPLAY_PERIOD); }
}
