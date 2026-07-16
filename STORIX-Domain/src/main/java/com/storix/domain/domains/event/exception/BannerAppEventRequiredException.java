package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerAppEventRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerAppEventRequiredException();

    private BannerAppEventRequiredException() { super(ErrorCode.EVENT_BANNER_APP_EVENT_REQUIRED); }
}
