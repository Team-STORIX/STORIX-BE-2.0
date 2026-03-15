package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class AlreadyJoinedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyJoinedException();

    private AlreadyJoinedException() {
        super(ErrorCode.ALREADY_JOINED_ROOM);
    }
}
