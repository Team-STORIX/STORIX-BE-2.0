package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminSignupPendingNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminSignupPendingNotFoundException();

    private AdminSignupPendingNotFoundException() { super(ErrorCode.ADMIN_SIGNUP_PENDING_NOT_FOUND); }
}
