package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UserAppEventPayloadSerializationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UserAppEventPayloadSerializationException();

    private UserAppEventPayloadSerializationException() { super(ErrorCode.APP_EVENT_PAYLOAD_SERIALIZATION_FAILED); }
}
