package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class IncompleteIdentityVerificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new IncompleteIdentityVerificationException();

    private IncompleteIdentityVerificationException() { super(ErrorCode.ADULT_VERIFICATION_NOT_COMPLETED); }
}
