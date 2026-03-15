package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReviewUpdateRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewUpdateRequestException();

  private InvalidReviewUpdateRequestException() { super(ErrorCode.REVIEW_UPDATE_INVALID_REQUEST);}
}
