package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateUserException();

    private DuplicateUserException() { super(ErrorCode.DUPLICATE_USER_SIGN); }
}