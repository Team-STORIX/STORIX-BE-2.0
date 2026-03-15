package com.storix.storix_api.global.apiPayload.exception.favorite;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateFavoriteWorksRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFavoriteWorksRequestException();

  private DuplicateFavoriteWorksRequestException() { super(ErrorCode.FAVORITE_WORKS_DUPLICATE_REQUEST);}
}
