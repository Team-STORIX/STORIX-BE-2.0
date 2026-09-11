package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class MissingBirthDateException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MissingBirthDateException();

    private MissingBirthDateException() { super(ErrorCode.ADULT_VERIFICATION_BIRTH_DATE_MISSING); }
}
