package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReviewReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewReportException();

  private InvalidReviewReportException() { super(ErrorCode.INVALID_REVIEW_USER_REPORT);}
}
