package com.storix.domain.domains.user.exception.token;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ExpiredOnboardingTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredOnboardingTokenException();

    private ExpiredOnboardingTokenException() { super(ErrorCode.ONBOARDING_TOKEN_EXPIRED); }
}