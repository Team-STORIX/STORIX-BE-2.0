package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() { super(ErrorCode.INVALID_TOKEN); }
}
