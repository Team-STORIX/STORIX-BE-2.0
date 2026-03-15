package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FeignClientUnauthorizedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientUnauthorizedException();

    private FeignClientUnauthorizedException() { super(ErrorCode.OTHER_SERVER_UNAUTHORIZED); }
}