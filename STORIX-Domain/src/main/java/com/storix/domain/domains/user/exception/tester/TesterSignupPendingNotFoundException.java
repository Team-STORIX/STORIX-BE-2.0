package com.storix.domain.domains.user.exception.tester;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TesterSignupPendingNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TesterSignupPendingNotFoundException();

    private TesterSignupPendingNotFoundException() { super(ErrorCode.TESTER_SIGNUP_PENDING_NOT_FOUND); }
}
