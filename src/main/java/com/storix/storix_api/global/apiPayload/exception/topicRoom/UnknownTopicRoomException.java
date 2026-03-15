package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownTopicRoomException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownTopicRoomException();
    private UnknownTopicRoomException() { super(ErrorCode.TOPIC_ROOM_NOT_FOUND);}
}
