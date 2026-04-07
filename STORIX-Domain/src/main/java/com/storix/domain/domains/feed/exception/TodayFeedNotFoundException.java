package com.storix.domain.domains.feed.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TodayFeedNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new TodayFeedNotFoundException();

  private TodayFeedNotFoundException() { super(ErrorCode.TODAY_FEED_NOT_FOUND);}
}
