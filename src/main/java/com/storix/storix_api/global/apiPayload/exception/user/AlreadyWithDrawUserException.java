package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class AlreadyWithDrawUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyWithDrawUserException();

    private AlreadyWithDrawUserException() { super(ErrorCode.INVALID_USER_WITHDRAW); }
}
