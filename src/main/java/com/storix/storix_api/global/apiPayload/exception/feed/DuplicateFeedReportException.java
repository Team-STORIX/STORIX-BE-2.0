package com.storix.storix_api.global.apiPayload.exception.feed;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateFeedReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFeedReportException();

  private DuplicateFeedReportException() { super(ErrorCode.DUPLICATE_FEED_USER_REPORT);}
}
