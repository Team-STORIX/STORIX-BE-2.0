package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class NotAdultVerifiedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new NotAdultVerifiedException();

    private NotAdultVerifiedException() { super(ErrorCode.ADULT_VERIFICATION_REQUIRED); }
}
