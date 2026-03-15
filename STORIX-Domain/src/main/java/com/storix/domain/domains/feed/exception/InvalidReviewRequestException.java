package com.storix.domain.domains.feed.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReviewRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewRequestException();

  private InvalidReviewRequestException() { super(ErrorCode.REVIEW_INVALID_REQUEST);}
}
