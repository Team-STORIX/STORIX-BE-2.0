package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateReviewReportException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateReviewReportException();

  private DuplicateReviewReportException() { super(ErrorCode.DUPLICATE_REVIEW_USER_REPORT);}
}
