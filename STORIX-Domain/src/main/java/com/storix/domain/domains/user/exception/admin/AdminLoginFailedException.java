package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminLoginFailedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminLoginFailedException();

    private AdminLoginFailedException() { super(ErrorCode.ADMIN_LOGIN_FAILED); }
}
