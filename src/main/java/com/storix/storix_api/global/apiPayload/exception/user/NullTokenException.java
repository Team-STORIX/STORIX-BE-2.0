package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class NullTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new NullTokenException();

    private NullTokenException() { super(ErrorCode.TOKEN_NOT_EXIST); }
}
