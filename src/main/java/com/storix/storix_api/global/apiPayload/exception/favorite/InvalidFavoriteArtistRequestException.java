package com.storix.storix_api.global.apiPayload.exception.favorite;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidFavoriteArtistRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidFavoriteArtistRequestException();

  private InvalidFavoriteArtistRequestException() { super(ErrorCode.FAVORITE_ARTIST_INVALID_REQUEST);}
}
