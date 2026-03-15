package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FeignClientForbiddenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientForbiddenException();

    private FeignClientForbiddenException() { super(ErrorCode.OTHER_SERVER_FORBIDDEN); }
}