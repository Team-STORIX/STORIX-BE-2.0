package com.storix.domain.domains.adultverification.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AlreadyProcessedAdultVerificationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyProcessedAdultVerificationException();

    private AlreadyProcessedAdultVerificationException() { super(ErrorCode.ADULT_VERIFICATION_ALREADY_PROCESSED); }
}
