package com.storix.storix_api.global.apiPayload.exception.feed;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidReviewRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewRequestException();

  private InvalidReviewRequestException() { super(ErrorCode.REVIEW_INVALID_REQUEST);}
}
