package com.storix.domain.domains.favorite.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateFavoriteArtistRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFavoriteArtistRequestException();

  private DuplicateFavoriteArtistRequestException() { super(ErrorCode.FAVORITE_ARTIST_DUPLICATE_REQUEST);}
}
