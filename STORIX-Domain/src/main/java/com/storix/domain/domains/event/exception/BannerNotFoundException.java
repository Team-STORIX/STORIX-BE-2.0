package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerNotFoundException();

    private BannerNotFoundException() { super(ErrorCode.EVENT_BANNER_NOT_FOUND); }
}
