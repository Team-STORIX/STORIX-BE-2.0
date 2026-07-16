package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannerImageNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannerImageNotExistException();

    private BannerImageNotExistException() { super(ErrorCode.EVENT_BANNER_IMAGE_NOT_EXIST); }
}
