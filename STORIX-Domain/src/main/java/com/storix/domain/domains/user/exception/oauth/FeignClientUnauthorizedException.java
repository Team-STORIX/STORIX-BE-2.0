package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FeignClientUnauthorizedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientUnauthorizedException();

    private FeignClientUnauthorizedException() { super(ErrorCode.OTHER_SERVER_UNAUTHORIZED); }
}