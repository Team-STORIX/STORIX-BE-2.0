package com.storix.domain.domains.favorite.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidFavoriteWorksRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidFavoriteWorksRequestException();

  private InvalidFavoriteWorksRequestException() { super(ErrorCode.FAVORITE_WORKS_INVALID_REQUEST);}
}
