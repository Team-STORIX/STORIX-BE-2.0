package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TodayTopicRoomNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new TodayTopicRoomNotFoundException();

  private TodayTopicRoomNotFoundException() { super(ErrorCode.TODAY_TOPIC_ROOM_NOT_FOUND);}
}
