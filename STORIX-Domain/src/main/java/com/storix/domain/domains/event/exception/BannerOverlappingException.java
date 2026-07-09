package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerOverlappingException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerOverlappingException();

    private BannerOverlappingException() { super(ErrorCode.EVENT_BANNER_OVERLAPPING); }
}
