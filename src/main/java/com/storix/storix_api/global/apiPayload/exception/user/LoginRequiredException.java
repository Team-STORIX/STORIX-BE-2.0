package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class LoginRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new LoginRequiredException();

    private LoginRequiredException() { super(ErrorCode.LOGIN_REQUIRED); }
}
