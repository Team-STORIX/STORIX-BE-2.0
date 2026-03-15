package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FeignClientExpiredTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientExpiredTokenException();

    private FeignClientExpiredTokenException() { super(ErrorCode.OTHER_SERVER_EXPIRED_TOKEN); }
}