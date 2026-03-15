package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnverifiedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnverifiedException();

    private UnverifiedException() { super(ErrorCode.ADULT_VERIFICATION_REQUIRED);}
}
