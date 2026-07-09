package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PopupNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PopupNotFoundException();

    private PopupNotFoundException() { super(ErrorCode.EVENT_POPUP_NOT_FOUND); }
}
