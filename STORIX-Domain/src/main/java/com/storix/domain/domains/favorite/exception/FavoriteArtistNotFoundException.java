package com.storix.domain.domains.favorite.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class FavoriteArtistNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new FavoriteArtistNotFoundException();

  private FavoriteArtistNotFoundException() { super(ErrorCode.FAVORITE_ARTIST_NOT_FOUND);}
}
