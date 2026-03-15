package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReviewDeleteRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewDeleteRequestException();

  private InvalidReviewDeleteRequestException() { super(ErrorCode.REVIEW_DELETE_INVALID_REQUEST);}
}
