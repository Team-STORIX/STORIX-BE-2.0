package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdultVerificationOwnerMismatchException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdultVerificationOwnerMismatchException();

    private AdultVerificationOwnerMismatchException() { super(ErrorCode.ADULT_VERIFICATION_OWNER_MISMATCH); }
}
