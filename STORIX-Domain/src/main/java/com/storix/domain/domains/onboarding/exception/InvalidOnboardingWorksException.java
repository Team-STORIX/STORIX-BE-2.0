package com.storix.domain.domains.onboarding.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidOnboardingWorksException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidOnboardingWorksException();

    private InvalidOnboardingWorksException() { super(ErrorCode.ONBOARDING_INVALID_WORKS); }
}
