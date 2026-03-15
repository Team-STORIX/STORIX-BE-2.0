package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnverifiedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnverifiedException();

    private UnverifiedException() { super(ErrorCode.ADULT_VERIFICATION_REQUIRED);}
}
