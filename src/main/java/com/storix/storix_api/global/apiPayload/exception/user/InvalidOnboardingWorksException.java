package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidOnboardingWorksException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidOnboardingWorksException();

    private InvalidOnboardingWorksException() { super(ErrorCode.ONBOARDING_INVALID_WORKS); }
}
