package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownReviewException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new UnknownReviewException();

  private UnknownReviewException() { super(ErrorCode.REVIEW_NOT_FOUND);}
}
