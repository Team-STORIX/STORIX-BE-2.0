package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SearchNoTopicRoomFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SearchNoTopicRoomFoundException();
    private SearchNoTopicRoomFoundException() { super(ErrorCode.SEARCH_NO_TOPIC_ROOM_FOUND); }
}
