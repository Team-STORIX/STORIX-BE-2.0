package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateReviewReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateReviewReportException();

  private DuplicateReviewReportException() { super(ErrorCode.DUPLICATE_REVIEW_USER_REPORT);}
}
