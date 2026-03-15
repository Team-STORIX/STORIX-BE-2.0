package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FeignClientExpiredTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientExpiredTokenException();

    private FeignClientExpiredTokenException() { super(ErrorCode.OTHER_SERVER_EXPIRED_TOKEN); }
}