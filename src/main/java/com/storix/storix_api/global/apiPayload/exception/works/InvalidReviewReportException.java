package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidReviewReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewReportException();

  private InvalidReviewReportException() { super(ErrorCode.INVALID_REVIEW_USER_REPORT);}
}
