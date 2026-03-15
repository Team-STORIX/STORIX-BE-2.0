package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownUserException();

    private UnknownUserException() { super(ErrorCode.NOT_FOUND); }
}