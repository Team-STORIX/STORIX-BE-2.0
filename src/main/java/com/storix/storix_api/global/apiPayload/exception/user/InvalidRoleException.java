package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidRoleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidRoleException();

    private InvalidRoleException() { super(ErrorCode.INVALID_ROLE_ERROR); }
}
