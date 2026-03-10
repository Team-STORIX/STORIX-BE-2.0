package com.storix.domain.domains.favorite.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateFavoriteWorksRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFavoriteWorksRequestException();

  private DuplicateFavoriteWorksRequestException() { super(ErrorCode.FAVORITE_WORKS_DUPLICATE_REQUEST);}
}
