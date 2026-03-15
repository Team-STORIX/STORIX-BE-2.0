package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class MaxLimitException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MaxLimitException();

    private MaxLimitException() { super(ErrorCode.TOPIC_ROOM_LIMIT_EXCEEDED);}
}
