package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class MinorNotAllowedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MinorNotAllowedException();

    private MinorNotAllowedException() { super(ErrorCode.ADULT_VERIFICATION_MINOR); }
}
