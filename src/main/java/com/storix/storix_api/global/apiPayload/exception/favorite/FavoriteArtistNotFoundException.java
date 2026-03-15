package com.storix.storix_api.global.apiPayload.exception.favorite;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class FavoriteArtistNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new FavoriteArtistNotFoundException();

  private FavoriteArtistNotFoundException() { super(ErrorCode.FAVORITE_ARTIST_NOT_FOUND);}
}
