package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PopupAppEventRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PopupAppEventRequiredException();

    private PopupAppEventRequiredException() { super(ErrorCode.EVENT_POPUP_APP_EVENT_REQUIRED); }
}
