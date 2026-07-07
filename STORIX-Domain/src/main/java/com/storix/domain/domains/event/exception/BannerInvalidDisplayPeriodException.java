package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerInvalidDisplayPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerInvalidDisplayPeriodException();

    private BannerInvalidDisplayPeriodException() { super(ErrorCode.EVENT_BANNER_INVALID_DISPLAY_PERIOD); }
}
