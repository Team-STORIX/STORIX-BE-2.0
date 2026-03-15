package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FeignClientServerErrorException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientServerErrorException();

    private FeignClientServerErrorException() { super(ErrorCode.OTHER_SERVER_INTERNAL_SERVER_ERROR); }
}