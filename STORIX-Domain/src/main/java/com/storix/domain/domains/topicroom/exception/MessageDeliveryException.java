package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class MessageDeliveryException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MessageDeliveryException();

    private MessageDeliveryException() { super(ErrorCode.CHAT_SERVER_ERROR); }
}
