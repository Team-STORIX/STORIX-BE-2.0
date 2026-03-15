package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FeignClientBadRequestException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientBadRequestException();

    private FeignClientBadRequestException() { super(ErrorCode.OTHER_SERVER_BAD_REQUEST); }
}