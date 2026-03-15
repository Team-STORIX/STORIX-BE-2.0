package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ArtistLoginException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ArtistLoginException();

    private ArtistLoginException() { super(ErrorCode.INVALID_CREDENTIALS); }
}