package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ArtistLoginException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ArtistLoginException();

    private ArtistLoginException() { super(ErrorCode.INVALID_CREDENTIALS); }
}