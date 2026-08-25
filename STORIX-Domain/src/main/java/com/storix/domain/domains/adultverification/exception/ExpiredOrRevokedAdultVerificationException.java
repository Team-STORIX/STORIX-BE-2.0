package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ExpiredOrRevokedAdultVerificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ExpiredOrRevokedAdultVerificationException();

    private ExpiredOrRevokedAdultVerificationException() { super(ErrorCode.ADULT_VERIFICATION_EXPIRED_OR_REVOKED); }
}
