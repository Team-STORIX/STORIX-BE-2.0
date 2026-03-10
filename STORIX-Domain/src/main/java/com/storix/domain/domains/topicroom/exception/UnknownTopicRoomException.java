package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownTopicRoomException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownTopicRoomException();
    private UnknownTopicRoomException() { super(ErrorCode.TOPIC_ROOM_NOT_FOUND);}
}
