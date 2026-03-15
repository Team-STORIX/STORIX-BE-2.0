package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownReviewException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new UnknownReviewException();

  private UnknownReviewException() { super(ErrorCode.REVIEW_NOT_FOUND);}
}
