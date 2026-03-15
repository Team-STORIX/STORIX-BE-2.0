package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidReviewUpdateRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidReviewUpdateRequestException();

  private InvalidReviewUpdateRequestException() { super(ErrorCode.REVIEW_UPDATE_INVALID_REQUEST);}
}
