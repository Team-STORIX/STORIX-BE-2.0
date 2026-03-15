package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownTopicRoomUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownTopicRoomUserException();
    private UnknownTopicRoomUserException() { super(ErrorCode.TOPIC_ROOM_USER_NOT_FOUND);}
}
