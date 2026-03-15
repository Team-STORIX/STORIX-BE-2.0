package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidTitleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidTitleException();

    private InvalidTitleException() {
        super(ErrorCode.INVALID_TOPIC_ROOM_TITLE);
    }
}
