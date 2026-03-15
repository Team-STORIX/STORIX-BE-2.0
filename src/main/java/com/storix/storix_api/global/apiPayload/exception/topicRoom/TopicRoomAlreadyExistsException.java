package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class TopicRoomAlreadyExistsException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TopicRoomAlreadyExistsException();

    private TopicRoomAlreadyExistsException() { super(ErrorCode.TOPIC_ROOM_ALREADY_EXISTS); }
}
