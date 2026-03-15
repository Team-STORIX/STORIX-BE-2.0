package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class MessageDeliveryException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MessageDeliveryException();

    private MessageDeliveryException() { super(ErrorCode.CHAT_SERVER_ERROR); }
}
