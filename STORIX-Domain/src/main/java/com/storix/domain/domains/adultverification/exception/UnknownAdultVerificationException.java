package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownAdultVerificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownAdultVerificationException();

    private UnknownAdultVerificationException() { super(ErrorCode.ADULT_VERIFICATION_NOT_FOUND); }
}
