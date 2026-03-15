package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnHandleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnHandleException();

    private UnHandleException() { super(ErrorCode.BAD_REQUEST); }
}