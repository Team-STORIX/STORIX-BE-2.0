package com.storix.domain.domains.user.exception.developer;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DeveloperSignupPendingNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DeveloperSignupPendingNotFoundException();

    private DeveloperSignupPendingNotFoundException() { super(ErrorCode.DEVELOPER_SIGNUP_PENDING_NOT_FOUND); }
}
