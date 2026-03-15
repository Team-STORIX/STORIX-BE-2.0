package com.storix.domain.domains.favorite.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidFavoriteArtistRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidFavoriteArtistRequestException();

  private InvalidFavoriteArtistRequestException() { super(ErrorCode.FAVORITE_ARTIST_INVALID_REQUEST);}
}
