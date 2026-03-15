package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class MaxLimitException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new MaxLimitException();

    private MaxLimitException() { super(ErrorCode.TOPIC_ROOM_LIMIT_EXCEEDED);}
}
