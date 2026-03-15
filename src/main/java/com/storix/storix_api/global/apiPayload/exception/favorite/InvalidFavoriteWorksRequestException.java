package com.storix.storix_api.global.apiPayload.exception.favorite;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidFavoriteWorksRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidFavoriteWorksRequestException();

  private InvalidFavoriteWorksRequestException() { super(ErrorCode.FAVORITE_WORKS_INVALID_REQUEST);}
}
