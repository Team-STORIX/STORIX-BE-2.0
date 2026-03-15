package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ExpiredOnboardingTokenException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredOnboardingTokenException();

    private ExpiredOnboardingTokenException() { super(ErrorCode.ONBOARDING_TOKEN_EXPIRED); }
}