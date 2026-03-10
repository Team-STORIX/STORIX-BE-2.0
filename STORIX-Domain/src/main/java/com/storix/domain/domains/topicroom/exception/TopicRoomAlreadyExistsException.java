package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TopicRoomAlreadyExistsException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TopicRoomAlreadyExistsException();

    private TopicRoomAlreadyExistsException() { super(ErrorCode.TOPIC_ROOM_ALREADY_EXISTS); }
}
