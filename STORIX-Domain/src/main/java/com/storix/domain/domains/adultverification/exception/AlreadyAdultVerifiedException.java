package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AlreadyAdultVerifiedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyAdultVerifiedException();

    private AlreadyAdultVerifiedException() { super(ErrorCode.ADULT_VERIFICATION_ALREADY_VERIFIED); }
}
