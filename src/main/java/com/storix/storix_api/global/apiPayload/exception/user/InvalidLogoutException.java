package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidLogoutException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidLogoutException();

    private InvalidLogoutException() { super(ErrorCode.INVALID_USER_LOGOUT); }
}
