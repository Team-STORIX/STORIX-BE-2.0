
package com.storix.storix_api.global.apiPayload.exception.feed;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateFeedReplyReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFeedReplyReportException();

  private DuplicateFeedReplyReportException() { super(ErrorCode.DUPLICATE_FEED_REPLY_USER_REPORT);}
}
