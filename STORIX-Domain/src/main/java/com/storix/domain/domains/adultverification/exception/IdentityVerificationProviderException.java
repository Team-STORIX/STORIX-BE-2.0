package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class IdentityVerificationProviderException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new IdentityVerificationProviderException();

    private IdentityVerificationProviderException() { super(ErrorCode.ADULT_VERIFICATION_PROVIDER_ERROR); }
}
