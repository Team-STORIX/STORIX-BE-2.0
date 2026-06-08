package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateTopicRoomReportException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateTopicRoomReportException();

    private DuplicateTopicRoomReportException() {
        super(ErrorCode.DUPLICATE_TOPIC_ROOM_REPORT);
    }
}
