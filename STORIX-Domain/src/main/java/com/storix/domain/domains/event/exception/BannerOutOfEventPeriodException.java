package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerOutOfEventPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerOutOfEventPeriodException();

    private BannerOutOfEventPeriodException() { super(ErrorCode.EVENT_BANNER_OUT_OF_EVENT_PERIOD); }
}
