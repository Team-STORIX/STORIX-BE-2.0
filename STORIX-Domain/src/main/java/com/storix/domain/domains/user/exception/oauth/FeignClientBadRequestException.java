package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FeignClientBadRequestException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientBadRequestException();

    private FeignClientBadRequestException() { super(ErrorCode.OTHER_SERVER_BAD_REQUEST); }
}