package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidReviewDeleteRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewDeleteRequestException();

  private InvalidReviewDeleteRequestException() { super(ErrorCode.REVIEW_DELETE_INVALID_REQUEST);}
}
