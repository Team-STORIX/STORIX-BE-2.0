package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidTitleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidTitleException();

    private InvalidTitleException() {
        super(ErrorCode.INVALID_TOPIC_ROOM_TITLE);
    }
}
