package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidTitleAdminKeywordException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidTitleAdminKeywordException();

    private InvalidTitleAdminKeywordException() {
        super(ErrorCode.TOPIC_ROOM_ADMIN_KEYWORD_TITLE);
    }
}
