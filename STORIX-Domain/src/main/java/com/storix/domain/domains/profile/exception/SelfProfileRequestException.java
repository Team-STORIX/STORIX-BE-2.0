package com.storix.domain.domains.profile.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SelfProfileRequestException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SelfProfileRequestException();

    private SelfProfileRequestException() {
        super(ErrorCode.PROFILE_SELF_REQUEST);
    }
}
