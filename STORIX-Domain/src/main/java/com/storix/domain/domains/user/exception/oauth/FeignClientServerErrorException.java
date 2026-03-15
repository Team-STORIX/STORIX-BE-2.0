package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FeignClientServerErrorException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientServerErrorException();

    private FeignClientServerErrorException() { super(ErrorCode.OTHER_SERVER_INTERNAL_SERVER_ERROR); }
}