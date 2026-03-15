package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FeignClientForbiddenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new FeignClientForbiddenException();

    private FeignClientForbiddenException() { super(ErrorCode.OTHER_SERVER_FORBIDDEN); }
}