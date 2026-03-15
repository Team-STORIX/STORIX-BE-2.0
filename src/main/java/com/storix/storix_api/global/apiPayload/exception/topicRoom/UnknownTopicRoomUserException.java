package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownTopicRoomUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownTopicRoomUserException();
    private UnknownTopicRoomUserException() { super(ErrorCode.TOPIC_ROOM_USER_NOT_FOUND);}
}
