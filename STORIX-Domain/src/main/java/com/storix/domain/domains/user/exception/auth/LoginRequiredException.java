package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class LoginRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new LoginRequiredException();

    private LoginRequiredException() { super(ErrorCode.LOGIN_REQUIRED); }
}
