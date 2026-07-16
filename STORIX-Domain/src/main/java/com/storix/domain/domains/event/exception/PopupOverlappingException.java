package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PopupOverlappingException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PopupOverlappingException();

    private PopupOverlappingException() { super(ErrorCode.EVENT_POPUP_OVERLAPPING); }
}
