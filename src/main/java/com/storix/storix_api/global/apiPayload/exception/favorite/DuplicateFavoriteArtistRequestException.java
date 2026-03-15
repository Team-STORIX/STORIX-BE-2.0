package com.storix.storix_api.global.apiPayload.exception.favorite;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateFavoriteArtistRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new DuplicateFavoriteArtistRequestException();

  private DuplicateFavoriteArtistRequestException() { super(ErrorCode.FAVORITE_ARTIST_DUPLICATE_REQUEST);}
}
